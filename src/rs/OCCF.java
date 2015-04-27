package rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Map.Entry;
import java.util.Set;

public class OCCF {

	public static Map<Integer, Set<Integer>> userItems = new HashMap<>();
	public static Map<Integer, Set<Integer>> itemUsers = new HashMap<>();
	// (item, item, similarity)
	public static Map<Integer, Map<Integer, Float>> itemSimilarity = new HashMap<>();
	public static Map<Integer, Queue<Float>>itemTopK = new HashMap<Integer, Queue<Float>>();
	final int k = 5;
	private Comparator<Float> cmp = new Comparator<Float>() {
        public int compare(Float e1, Float e2) {
            return  e1.compareTo(e2);  //  浮点数要调用该函数，自己实现出错,  10 9 8 7 6
        }
    };

	void readFile(String fileName){
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
					}
					userItems.get(userIntegers[0]).add(userIntegers[1]);
					if(!itemUsers.containsKey(userIntegers[1])){
						itemUsers.put(userIntegers[1], new HashSet<Integer>());
					}
					itemUsers.get(userIntegers[1]).add(userIntegers[0]);
				}
				reader.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void getSimilarity(){
		Set<Integer>itemK = new HashSet<Integer>();
		Set<Integer>itemJ = new HashSet<Integer>();
		Set<Integer>intersection = new HashSet<Integer>();  //  交集
		Set<Integer>unionSet = new HashSet<Integer>();	//  并集

		for( Integer keyString : itemUsers.keySet()){
			//  itemK, item分别对应item k和j 相应的set集合
			itemK.clear();
			itemK.addAll(itemUsers.get(keyString));
			//System.out.println("itemk" + itemK);
			unionSet.clear();
			unionSet.addAll(itemK);
			//System.out.println("并集" + unionSet);
			intersection.clear();
			intersection.addAll(itemK);
			//System.out.println("交集" + intersection);
			for( Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){
				if(e.getKey().equals(keyString)){
					continue;
				}
				else {
					itemJ = e.getValue();
					//  对集合itemK, itemJ 求交集与并集，并计算jaccard相似度
					intersection.retainAll(itemJ);  //  获得交集
					if(intersection.size() != 0)
						System.out.println( "k " + keyString + " j " + e.getKey() + " 交集 " + intersection);
					unionSet.addAll(itemJ);  //  获得并集
					//System.out.println( "k " + keyString + " j " + e.getKey() + " 并集 " + unionSet);
					Map<Integer, Float>tempMap = new HashMap<Integer, Float>();
					//System.out.println("交集大小：" + intersection.size() + " 并集大小：" + unionSet.size());
					tempMap.put(e.getKey(), (float)intersection.size()/(float)unionSet.size());
					if(tempMap.get(e.getKey()) != 0.0)
					System.out.println("k " + keyString + " j " + e.getKey() + " 相似度" + tempMap.get(e.getKey()));
					itemSimilarity.put(keyString, tempMap);

				}
			}
		}
	}

	void selectTopK(){

		for( Entry<Integer, Map<Integer, Float>> e : itemSimilarity.entrySet()){
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
			//System.out.println(itemTopK.get(e.getKey()));
		}
	}

	void itemBaseOCCF(){

	}

	void t(){
		 Queue<Integer> qi = new PriorityQueue<Integer>();

	        qi.add(5);
	        qi.add(2);
	        qi.add(1);
	        qi.add(10);
	        qi.add(3);

	        while (!qi.isEmpty()) {
	            System.out.print(qi.poll() + ",");
	        }
	        System.out.println();
	        System.out.println("-----------------------------");
	              // <span></span><span>自定义的比较器，可以让我们自由定义比较的顺序</span> Comparator<Integer> cmp;

	        Queue<Float> q2 = new PriorityQueue<Float>(cmp);
	        q2.add((float) 3.7);
	        q2.add((float) 8.9);
	        q2.add((float) 9);
	        q2.add((float) 1.2);
	        while (!q2.isEmpty()) {
	            System.out.print(q2.poll() + ",");
	        }
	        System.out.println();
		Set<Integer> result = new HashSet<Integer>();
        Set<Integer> set1 = new HashSet<Integer>(){{
            add(1);
            add(3);
            add(5);
        }};

        Set<Integer> set2 = new HashSet<Integer>(){{
            add(1);
            add(2);
            add(3);
        }};

        result.clear();
        System.out.println("1" + result);
        result.addAll(set1);
        System.out.println("2" + result);
        result.retainAll(set2);
        System.out.println("交集："+result);

        result.clear();
        result.addAll(set1);
        result.removeAll(set2);
        System.out.println("差集："+result);

        result.clear();
        result.addAll(set1);
        result.addAll(set2);
        System.out.println("并集："+result);
	}
	public static void main(String[] args){
		OCCF test = new OCCF();

		long startTime = System.currentTimeMillis();
		//test.t();
		test.readFile("u1.base.OCCF");
		test.getSimilarity();
		test.selectTopK();
		long endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
	}

}
