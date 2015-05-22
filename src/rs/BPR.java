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

<<<<<<< Updated upstream
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
=======
	//  (user, preferred items), (item, preferred users)
	public static Map<Integer, Set<Integer>> userItems = new HashMap<>();
	public static Map<Integer, Set<Integer>> itemUsers = new HashMap<>();
	
    public static Map<Integer, Map<Float, Integer>>ratingHat = new HashMap<Integer, Map<Float,Integer>>();

    //  d = latent dimensions, T = iteration number
    static final int d = 10, T = 500;
    //  n = users, m = items
    static final int n = 943, m = 1682;
    static final int pre_k = 5;


    public static float[] itemBias = new float[m];
    public static float[][] matrixU = new float[n][d];
    public static float[][] matrixV = new float[m][d];

    public static int[] userArr;
    public static int[] itemArr;

    int recordNum = 0;
    float μ;

    //  read file and initialization of modal parameters
    void init(String fileName){
        try {
            //  read file
            BufferedReader reader = new BufferedReader(new FileReader(new File(fileName)));
            String lineString;
            String[] userStrings;
            int[] userIntegers;

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
				
                recordNum++;

            }/*  end of reading file */
            reader.close();

            μ = (float)recordNum/n/m;

            userArr = new int[recordNum];
            itemArr = new int[recordNum];

            //  get item bias and initialize matrix item(matrixV)
            for(Entry<Integer, Set<Integer>> e : itemUsers.entrySet()){

                itemBias[e.getKey() - 1] = (float) (e.getValue().size()/n) - μ;

                //  initialize matrix item
                for(int k=0; k<d; ++k){
                    matrixV[e.getKey() - 1][k] = (float) ((float) (Math.random() - 0.5) * 0.01);
                }
            }
            int index = 0;
            for(Entry<Integer, Set<Integer>> e : userItems.entrySet()){

				for(Integer ee : e.getValue()){

                    userArr[index] = e.getKey();
            		itemArr[index] = ee;
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
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
=======
        for(int i=0; i<T; ++i){
            for(int j=0; j<recordNum; ++j){

                //  randomly pick up a pair(u, i)∈R
                int randomNum = (int) (Math.random() * recordNum);
                int randomUser = userArr[randomNum];
                int randomItem_i = itemArr[randomNum];

                //  randomly pick up an item j from I\Iu
                Set<Integer>itemSet = itemUsers.keySet();
                itemSet.retainAll((Set) userItems.get(randomUser));
                Object[] itemJArr = itemSet.toArray();
                System.out.println(itemJArr.length);
                int randomItem_j = (int) itemJArr[(int) (Math.random() * itemJArr.length)];

                //  calculate the gradients
                float uv = 0;
                for(int k=0; k<d; ++k){
                    uv += matrixU[randomUser -1][k] * matrixV[randomItem_i - 1][k];
                }
                double ratingHat_ui = uv + itemBias[randomItem_i-1];
                uv = 0;
                for(int k=0; k<d; ++k){
                	uv += matrixU[randomUser-1][k] * matrixV[randomItem_j-1][k];
                }
                double ratingHat_uj = uv + itemBias[randomItem_j-1];
                double ratingHat_uij = ratingHat_ui - ratingHat_uj;

                double[] deltaMatrixU = new double[d];
                double part1 = -1 * sigmoid(-1 * ratingHat_uij);
                for(int k=0; k<d; ++k){
                    deltaMatrixU[k] = part1 * (matrixV[randomItem_i-1][k] - matrixV[randomItem_j-1][k])
                    		+ alphaU * matrixU[randomUser-1][k];
                }
                double[] deltaMatrixV_i = new double[d];
                for(int k=0; k<d; ++k){
                    deltaMatrixV_i[k] = part1 * matrixU[randomUser-1][k] + alphaV * matrixV[randomItem_i-1][k];
                }

                double[] deltaMatrixV_j = new double[d];
                for(int k=0; k<d; ++k){
                    deltaMatrixV_j[k] = -1 * part1 * matrixU[randomUser-1][k] + alphaV * matrixV[randomItem_j-1][k];
                }

                double deltaItemBias_i = part1 + betaV * itemBias[randomItem_i-1];
                double deltaItemBias_j = -1 * part1 + betaV * itemBias[randomItem_j-1];

                //  update parameters

                itemBias[randomItem_i - 1] -= gamma * deltaItemBias_i;
                itemBias[randomItem_i - 1] -= gamma * deltaItemBias_j;
                for(int k=0; k<d; ++k){
                    matrixU[randomUser - 1][k] -= gamma * deltaMatrixU[k];
                    matrixV[randomItem_i - 1][k] -= gamma * deltaMatrixV_i[k];
                    matrixV[randomItem_j - 1][k] -= gamma * deltaMatrixV_j[k];
                }
            }
        }
>>>>>>> Stashed changes
	}

	void getPrediction(){
		for(Entry<Integer, Set<Integer>> e : userItems.entrySet()){
<<<<<<< Updated upstream
			
			if(!ratingHat.containsKey(e.getKey())){
				ratingHat.put(e.getKey(), new TreeMap<Float, Integer>());
			}
			float minPrediction = 0;
			for(Integer item : itemUsers.keySet()){
				//  user对该item买过就跳过
				if(e.getValue().contains(item)){
					continue;
				}
=======
			if(!ratingHat.containsKey(e.getKey())){
				ratingHat.put(e.getKey(), new TreeMap<Float, Integer>());
			}
			for(Entry<Integer, Set<Integer>> ee : itemUsers.entrySet()){
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
=======
				Float ratingHat_ui = uv + itemBias[ee.getKey()-1];
				
				if(ratingHat.get(e.getKey()).size() < pre_k){
					ratingHat.get(e.getKey()).put(ratingHat_ui, ee.getKey());
				}
				else{
					if(ratingHat_ui.compareTo(
							((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).firstEntry().getKey()
							) > 0){
						((TreeMap<Float, Integer>) ratingHat.get(e.getKey())).pollFirstEntry();
						ratingHat.get(e.getKey()).put(ratingHat_ui, ee.getKey());
					}
				}
>>>>>>> Stashed changes
			}
			System.out.println(ratingHat.get(e.getKey()));
		}
	}
<<<<<<< Updated upstream
	
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

=======

	void  evalutionMetrics(String testFile){
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
					
					if(ratingHat.containsKey(e.getKey())){
						
						float tempSum = 0;
						for( Integer ee : ratingHat.get(e.getKey()).values()){
							if(e.getValue().contains(ee)){
								tempSum++;
							}
						}
						preSum += tempSum/pre_k;
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



	public static void main(String[] args) {
		BPR test = new BPR();
		long startTime = System.currentTimeMillis();
		test.init("u1.base.OCCF");
		test.doMainJob();
		test.getPrediction();
		test.evalutionMetrics("u1.test.OCCF");
		long endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
>>>>>>> Stashed changes
	}

}
