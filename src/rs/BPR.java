package rs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class BPR {

	private static Map<Integer, Set<Integer>>userItems = new HashMap<Integer, Set<Integer>>();
	private static Map<Integer, Set<Integer>>itemUsers = new HashMap<Integer, Set<Integer>>();
	
	private static Map<Integer, Map<Float, Integer>>ratingHat = new HashMap<Integer, Map<Float,Integer>>();

	// n = userNum, m = itemNum
	private final int n = 943, m = 1682;
	//  d = matrix dimension, T = iterationNum
	private final int d = 20, T = 500;
	private final int pre_k = 5;
	int recordNum = 0;
	float miu;

	private float[][] matrixU = new float[n][d];
	private float[][] matrixV = new float[m][d];

	private float[] itemBias = new float[m];

	private int[] userRecordArr;
	private int[] itemRecordArr;

	void init(String trainFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(trainFile)));
			String linesString;
			String[] userItemStrings;
			try {
				while((linesString = reader.readLine()) != null){
					userItemStrings = linesString.split("[^0-9]");
					int user = Integer.parseInt(userItemStrings[0]);
					int item = Integer.parseInt(userItemStrings[1]);
					if(!userItems.containsKey(user)){
						userItems.put(user, new HashSet<Integer>());
					}
					userItems.get(user).add(item);
					if(!itemUsers.containsKey(item)){
						itemUsers.put(item, new HashSet<Integer>());
					}
					itemUsers.get(item).add(user);
					recordNum++;
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			//  use the statistics of training data to initialize the modal parameters
			miu = (float)recordNum/n/m;
			for(Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){
				itemBias[e.getKey()-1] = (float)(e.getValue().size())/n - miu;
				for(int i=0; i<d; ++i){
					matrixV[e.getKey()-1][i] = (float) ((Math.random() * 0.5) * 0.01);
				}
			}
			int index = 0;
			userRecordArr = new int[recordNum];
			itemRecordArr = new int[recordNum];
			for(Entry<Integer, Set<Integer>> e : userItems.entrySet()){
				for(int i=0; i<d; ++i){
					matrixU[e.getKey()-1][i] = (float) ((Math.random() * 0.5) * 0.01);
				}
				for(Integer itemInteger : e.getValue()){
					userRecordArr[index] = e.getKey();
					itemRecordArr[index] = itemInteger;
					++index;
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	double sigmoid(double f){
    	return (1.0/(1 + Math.pow(java.lang.Math.E, -1 * f)));
    }

	void doMainJob(){
		double gamma = 0.01;
        double alphaU = 0.01, alphaV = 0.01, betaV = 0.01;
		for(int i=0; i<T; ++i){
			for(int j=0; j<recordNum; ++j){
				//  randomly pick up a record(u, i)
				int randomNum = (int)(Math.random()*recordNum);
				int random_u = userRecordArr[randomNum];
				int random_i = itemRecordArr[randomNum];

				//  randomly pick up an item j from I\Iu
				Vector<Integer>randomSetItem_j = new Vector<Integer>(itemUsers.size()-userItems.get(random_u).size());
				for(Integer itemInteger : itemUsers.keySet()){
					if(userItems.get(random_u).contains(itemInteger)){
						continue;
					}
					randomSetItem_j.add(itemInteger);
				}
				int random_j = randomSetItem_j.get((int)(Math.random() * randomSetItem_j.size()));

				assert randomSetItem_j.size() == itemUsers.size()-userItems.get(random_u).size();

				random_u--;
				random_i--;

				//  calculate the gradients
				float uv = 0;
				for (int k = 0; k < d; k++) {
					uv += matrixU[random_u][k] * matrixV[random_i][k];
				}
				float ratingHat_ui = uv + itemBias[random_i];
				uv = 0;
				for (int k = 0; k < d; k++) {
					uv += matrixU[random_u][k] * matrixV[random_j][k];
				}
				float raingHat_uj = uv + itemBias[random_j];

				float ratingHat_uji = raingHat_uj - ratingHat_ui;
				float[] deltaMatrixU = new float[d];
				float factor = (float) sigmoid(ratingHat_uji);
				for(int k = 0; k < d; k++){
					deltaMatrixU[k] = (float) ((-1 * factor)*
							(matrixV[random_i][k] - matrixV[random_j][k]) + alphaU * matrixU[random_u][k]);
				} 
				float[] deltaMatrixV_i = new float[d];
				for (int k = 0; k < d; k++) {
					deltaMatrixV_i[k] = (float) ((-1 * factor * matrixU[random_u][k]) + alphaV * matrixV[random_i][k]);
				}
				float[] deltaMatrixV_j = new float[d];
				for (int k = 0; k < d; k++) {
					deltaMatrixV_j[k] = (float) ((factor * matrixU[random_u][k]) + alphaV * matrixV[random_j][k]);
				}
				float deltaItemBias_i = (float) ((-1 * factor) + betaV * itemBias[random_i]);
				float deltaItemBias_j = (float) (factor + betaV * itemBias[random_j]);

				//  update rules
				for (int k = 0; k < d; k++) {
					matrixU[random_u][k] -= gamma * deltaMatrixU[k]; 
				}
				for (int k = 0; k < d; k++) {
					matrixV[random_i][k] -= gamma * deltaMatrixV_i[k];
				}
				for (int k = 0; k < d; k++) {
					matrixV[random_j][k] -= gamma * deltaMatrixV_j[k];
				}
				itemBias[random_i] -= gamma * deltaItemBias_i;
				itemBias[random_j] -= gamma * deltaItemBias_j;
			}
		}
	}

	void getPrediction(){
		for(Entry<Integer, Set<Integer>> e : userItems.entrySet()){
			
			if(!ratingHat.containsKey(e.getKey())){
				ratingHat.put(e.getKey(), new TreeMap<Float, Integer>());
			}
			float minPrediction = 0;
			for(Integer item : itemUsers.keySet()){
				//  user对该item买过就跳过
				if(e.getValue().contains(item)){
					continue;
				}
				float uv = 0;
				for(int i=0; i<d; ++i){
					uv += matrixU[e.getKey()-1][i] * matrixV[item-1][i];
				}
				float curPrediction = uv + itemBias[item-1];
				if(ratingHat.get(e.getKey()).size() < pre_k){
					ratingHat.get(e.getKey()).put(curPrediction, item);
					minPrediction = ((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).firstKey();
				}
				else if((new Float(curPrediction)).compareTo(minPrediction) > 0){
					((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).pollFirstEntry();
					ratingHat.get(e.getKey()).put(curPrediction, item);
				}
			}
			System.out.println(ratingHat.get(e.getKey()));
		}
	}
	
	void evaluationMertrics(String testFile){
		try {
			BufferedReader reader = new BufferedReader(new FileReader(new File(testFile)));
			String lineString;
			String[] userItemArr;
			Map<Integer, Set<Integer>>userItemsTest = new HashMap<Integer, Set<Integer>>();
			Map<Integer, Set<Integer>>itemUsersTest = new HashMap<Integer, Set<Integer>>();
			try {
				while ((lineString = reader.readLine()) != null) {
					userItemArr = lineString.split("[^0-9]");
					if(!userItemsTest.containsKey(Integer.parseInt(userItemArr[0]))){
						userItemsTest.put(Integer.parseInt(userItemArr[0]), new HashSet<Integer>());
					}
					userItemsTest.get(Integer.parseInt(userItemArr[0])).add(Integer.parseInt(userItemArr[1]));
					if(!itemUsersTest.containsKey(Integer.parseInt(userItemArr[1]))){
						itemUsersTest.put(Integer.parseInt(userItemArr[1]), new HashSet<Integer>());
					}
					itemUsersTest.get(Integer.parseInt(userItemArr[1])).add(Integer.parseInt(userItemArr[0]));
				}
				reader.close();
				float preSum = 0;
				int preUserNum = 0;
				for(Entry<Integer, Set<Integer>> e : userItemsTest.entrySet()){
					//  如果对该user有预测
					if(ratingHat.containsKey(e.getKey())){
						preUserNum++;
						for( Entry<Float, Integer> ee : ratingHat.get(e.getKey()).entrySet()){
							int temp = 0;
							if(e.getValue().contains(ee.getValue())){
								temp++;
							}
							preSum += (float)temp/pre_k;
						}
					}
				}
				//preSum /= pre_k;
				System.out.println("Pre@k : " + (float)preSum/preUserNum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
	}
 	public static void main(String[] args) {
 		BPR test = new BPR();
 		System.out.println("BPR:");
        System.out.println("d = " + test.d);
        long startTime, endTime;
        startTime = System.currentTimeMillis();
        test.init("u1.base.OCCF");
        endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
        startTime = System.currentTimeMillis();
        test.doMainJob();
        endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
        startTime = System.currentTimeMillis();
        test.getPrediction();
        endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
        test.evaluationMertrics("u1.test.OCCF");
        endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );

	}

}
