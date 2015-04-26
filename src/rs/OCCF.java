package rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
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
		Set<Integer>itemK;
		Set<Integer>itemJ;
		Set<Integer>intersection;  //  交集
		Set<Integer>unionSet;	//  并集

		for( Integer keyString : itemUsers.keySet()){
			//  itemK, item分别对应item k和j 相应的set集合
			itemK = itemUsers.get(keyString);
			unionSet = itemK;
			intersection = itemK;
			for( Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){
				if(e.getKey().equals(keyString)){
					continue;
				}
				else {
					itemJ = e.getValue();
					//  对集合itemK, itemJ 求交集与并集，并计算jaccard相似度
					intersection.retainAll(itemJ);  //  获得交集
					unionSet.addAll(itemJ);
					Map<Integer, Float>tempMap = new HashMap<Integer, Float>();
					tempMap.put(e.getKey(), (float)intersection.size()/unionSet.size());
					itemSimilarity.put(keyString, tempMap);
				}
			}
		}
	}

	void selectTopK(){
		for( Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){

		}
	}

	void itemBaseOCCF(){

	}

	void t(){
		Queue<Float>topK = new PriorityQueue<Float>();
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
        result.addAll(set1);
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
		test.t();
	}

}
