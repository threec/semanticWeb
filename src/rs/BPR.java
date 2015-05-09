package rs;

import java.awt.ItemSelectable;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Set;

public class BPR {

	//  userItems = (user, item, rating), itemUsers = (item,user, rating)
    public static Map<Integer, Map<Integer, Integer>>userItems = new HashMap<Integer,Map<Integer, Integer>>();
    public static Map<Integer, Map<Integer, Integer>>itemUsers = new HashMap<Integer, Map<Integer, Integer>>();

    public static Map<Integer, Map<Float, Integer>>ratingHat = new HashMap<Integer, Map<Float,Integer>>();

    //  d = latent dimensions, T = iteration number
    static final int d = 10, T = 500;
    //  n = users, m = items
    static final int n = 943, m = 1682;



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
            Integer[] userIntegers;

            while ( (lineString = reader.readLine()) != null){

                userStrings = lineString.split("[^0-9]");
                userIntegers = new Integer[userStrings.length - 1];
                for(int i=0; i<userIntegers.length; ++i){
                    userIntegers[i] = Integer.valueOf(userStrings[i]);
                }

                if(!userItems.containsKey(userIntegers[0])){
                    userItems.put(userIntegers[0], new HashMap<Integer, Integer>());
                }
                userItems.get(userIntegers[0]).put(userIntegers[1], userIntegers[2]);

                if(!itemUsers.containsKey(userIntegers[1])){
                    itemUsers.put(userIntegers[1], new HashMap<Integer, Integer>());
                }
                itemUsers.get(userIntegers[1]).put(userIntegers[0], userIntegers[2]);

                recordNum++;

            }/*  end of reading file */
            reader.close();

            μ = (float)recordNum/n/m;

            userArr = new int[recordNum];
            itemArr = new int[recordNum];

            //  get item bias and initialize matrix item(matrixV)
            for(Map.Entry<Integer, Map<Integer, Integer>> e : itemUsers.entrySet()){

                itemBias[e.getKey() - 1] = (float) (e.getValue().size()/n) - μ;

                //  initialize matrix item
                for(int k=0; k<d; ++k){
                    matrixV[e.getKey() - 1][k] = (float) ((float) (Math.random() - 0.5) * 0.01);
                }
            }
            int index = 0;
            for(Entry<Integer, Map<Integer, Integer>> e : userItems.entrySet()){

            	for(Map.Entry<Integer, Integer> ee : e.getValue().entrySet()){

                    userArr[index] = e.getKey();
            		itemArr[index] = ee.getKey();

            		++index;
                }
            	//  initialize matrix user
                for(int k=0; k<d; ++k){
                    matrixU[e.getKey() - 1][k] = (float) ((float) (Math.random() - 0.5) * 0.01);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    double sigmoid(Double f){
    	return (1/(1 + Math.pow(java.lang.Math.E, -1 * f)));
    }

	void doMainJob(){
		double gamma = 0.01;
        double alphaU = 0.01, alphaV = 0.01, betaV = 0.01;

        for(int i=0; i<T; ++i){
            for(int j=0; j<recordNum; ++j){

                //  randomly pick up a pair(u, i)∈R
                int randomNum = (int) (Math.random() * recordNum);
                int randomUser = userArr[randomNum];
                int randomItem_i = itemArr[randomNum];

                //  randomly pick up an item j from I\Iu
                Set<Integer>itemSet = itemUsers.keySet();
                itemSet.retainAll((Collection<?>) userItems.get(randomUser));
                Object[] itemJArr = itemSet.toArray();
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
	}

	void getPrediction(){
		for(Entry<Integer, Map<Integer, Integer>> e : userItems.entrySet()){
			if(!ratingHat.containsKey(e.getKey())){
				ratingHat.put(e.getKey(), new TreeMap<Float, Integer>());
			}
			for(Entry<Integer, Map<Integer, Integer>> ee : itemUsers.entrySet()){
				float uv = 0;
				for(int k=0; k<d; ++k){
					uv += matrixU[e.getKey()-1][k] + matrixV[ee.getKey()-1][k];
				}
				float raingHat_ui = uv + itemBias[ee.getKey()-1];
				if()
			}
		}
	}

	void  evalutionMetrics(){

	}



	public static void main(String[] args) {


	}

}
