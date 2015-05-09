package rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

public class OCCF {
	//  (user, preferred items), (item, preferred users)
	public static Map<Integer, Set<Integer>> userItems = new HashMap<>();
	public static Map<Integer, Set<Integer>> itemUsers = new HashMap<>();
	// (item, item, similarity)
	public static Map<Integer, Map<Integer, Float>> itemSimilarity = new HashMap<>();
	public static Map<Integer, Map<Float, Integer>>itemTopK = new HashMap<>();
	public static Map<Integer, Map<Float, Integer>>ratingHat = new HashMap<>();
	final int k = 5;

    // read train file
	void init(String fileName){

		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
			String lineString;
			String[] userStrings;
			int[] userIntegers;
			try {
				while((lineString = reader.readLine()) != null){
					userStrings = lineString.split("[^0-9]");
					userIntegers = new int[2];
					for(int i=0; i<2; ++i){
						userIntegers[i] = Integer.valueOf(userStrings[i]);
					}

					if(!userItems.containsKey(userIntegers[0])){
						userItems.put(userIntegers[0], new HashSet<Integer>());
						ratingHat.put(userIntegers[0], new TreeMap<Float, Integer>());
					}
					userItems.get(userIntegers[0]).add(userIntegers[1]);


					if(!itemUsers.containsKey(userIntegers[1])){
						itemUsers.put(userIntegers[1], new HashSet<Integer>());
					}
					itemUsers.get(userIntegers[1]).add(userIntegers[0]);
				}
				reader.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
	}

	void getSimilarity(){

		for(Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){
			
			Set<Integer>itemUserSet_k = e.getValue();

			for(Entry<Integer, Set<Integer>> ee : itemUsers.entrySet()){
				if(e.getKey().equals(ee.getKey())){
					continue;
				}
				//  获得交集
				Set<Integer>intersection = new HashSet<Integer>();
				for(Integer i : itemUserSet_k){
					if(ee.getValue().contains(i)){
						intersection.add(i);
					}
				}

				//  交集为空的话就跳过，不为空则加入
				if(intersection.isEmpty()){
					continue;
				}
				//System.out.println(intersection.size());
				
				if(!itemSimilarity.containsKey(e.getKey())){
					itemSimilarity.put(e.getKey(), new HashMap<Integer, Float>());
				}

				//  获得并集
				Set<Integer>unionSet = new HashSet<Integer>();
				unionSet.addAll(e.getValue());
				unionSet.addAll(ee.getValue());
				
				itemSimilarity.get(e.getKey()).put(ee.getKey(), (float) ((double)intersection.size()/unionSet.size()));
			}
		}
	}

	void selectTopK(){

		/*
		for( Entry<Integer, Map<Integer, Float>> e : itemSimilarity.entrySet()){
			//  构造一个升序的优先队列
			Queue<Float>pQueue = new PriorityQueue<Float>(cmp);
			if(!itemTopK.containsKey(e.getKey())){
				itemTopK.put(e.getKey(), pQueue);
			}

			for( Entry<Integer, Float> ee : e.getValue().entrySet()){
				if(pQueue.size() < k){
					pQueue.add(ee.getValue());
				}
				else{
					//  如果队列满了并且后来的值比队列里面最小的的值大，就替换掉最小的值
					if(ee.getValue().compareTo(pQueue.peek()) > 0){
						pQueue.poll();
						pQueue.add(ee.getValue());
					}
				}
			}
		}
		*/
		for(Entry<Integer, Map<Integer, Float>> e : itemSimilarity.entrySet()){

			if(!itemTopK.containsKey(e.getValue())){
				itemTopK.put(e.getKey(), new TreeMap<Float, Integer>());
			}
			int i = 0;
			for(Entry<Integer, Float> ee : e.getValue().entrySet()){
				if(i < k){
					itemTopK.get(e.getKey()).put(ee.getValue(), ee.getKey());
					++i;
				}
				else {
					//  topk已满，若比最小映射的键大，就删除最小映射，加入当前映射关系
					if(ee.getValue().compareTo(
							((TreeMap<Float, Integer>) itemTopK.get(e.getKey())).firstEntry().getKey()
							) > 0){
						((TreeMap<Float, Integer>) itemTopK.get(e.getKey())).pollFirstEntry();
						itemTopK.get(e.getKey()).put(ee.getValue(), ee.getKey());
					}
				}
			}
			//System.out.println(itemTopK.get(e.getKey()));
		}
	}

	void itemBaseOCCF(){
		for(Entry<Integer, Set<Integer>> e : userItems.entrySet()){
			//  user对应的item集合
			Set<Integer>userItemSet = e.getValue();
			for(Entry<Integer, Map<Float, Integer>> ee : itemTopK.entrySet()){
				//获取user对应的item集合与里面item对应的相似topk的交集
				userItemSet.retainAll(ee.getValue().values());
				
				if(userItemSet.isEmpty()){
					continue;
				}
				assert userItemSet.size() <= e.getValue().size();

				float sum = 0;
				for(int i : userItemSet){
					sum += itemSimilarity.get(i).get(ee.getKey());
				}
				if(ratingHat.get(e.getKey()).size() < k){
					ratingHat.get(e.getKey()).put(sum, ee.getKey());
				}
				else {
					if(((TreeMap<Float, Integer>) ee.getValue()).firstEntry().getKey().compareTo(
							((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).firstEntry().getKey()) > 0){
						((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).pollFirstEntry();
						ratingHat.get(e.getKey()).put(sum, ee.getKey());
					}
				}
			}
			/*
			for(Entry<Integer, Float> f : ratingHat.get(e.getKey()).entrySet()){
				if(f.getValue().compareTo(0f) != 0){
					System.out.print(e.getKey());
					System.out.print(" " + ratingHat.get(e.getKey()));
					System.out.println();
				}
			}
			*/
		}

	}

	void evalutionMetrics(String testFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(testFile)));
			Map<Integer, Set<Integer>>userTest = new HashMap<Integer, Set<Integer>>();
 			String lineString;
			String[] userStrings;
			int[] userIntegers;
			float preSum = 0;
			try {
				while((lineString = reader.readLine()) != null){
					userStrings = lineString.split("[^0-9]");
					userIntegers = new int[2];
					for(int i=0; i<2; ++i){
						userIntegers[i] = Integer.valueOf(userStrings[i]);
					}
					if(!userTest.containsKey(userIntegers[0])){
						userTest.put(userIntegers[0], new HashSet<Integer>());
					}
					userTest.get(userIntegers[0]).add(userIntegers[1]);
				}
				reader.close();
				for( Entry<Integer, Set<Integer>> e : userTest.entrySet()){
					//System.out.print(e.getKey());
					//System.out.print("key;  ");
					//System.out.print(e.getValue());
					//System.out.println();
					if(ratingHat.containsKey(e.getKey())){
						//System.out.println("baohan ");
						float tempSum = 0;
						for( Integer ee : ratingHat.get(e.getKey()).values()){
							if(e.getValue().contains(ee)){
								tempSum++;
							}
						}
						preSum += tempSum/k;
					}
				}
				//System.out.println(preSum);
				System.out.println("Pre@k : " + (float)preSum/userTest.size());
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		}
	}


	public static void main(String[] args){
		OCCF test = new OCCF();

		long startTime = System.currentTimeMillis();

		test.init("u1.base.OCCF");
		
		test.getSimilarity();
		test.selectTopK();
		test.itemBaseOCCF();
		test.evalutionMetrics("u1.test.OCCF");
		long endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
	}

}
