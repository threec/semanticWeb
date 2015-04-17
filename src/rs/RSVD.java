package rs;

import java.io.*;
import java.net.Inet4Address;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.IntPredicate;

/**
 * Created by xlc on 2015/4/10.
 */
public class RSVD {
    //  userItems = (user, item, rating), itemUsers = (item,user, rating)
    public static Map<Integer, Map<Integer, Integer>>userItems = new HashMap<Integer,Map<Integer, Integer>>();
    public static Map<Integer, Map<Integer, Integer>>itemUsers = new HashMap<Integer, Map<Integer, Integer>>();
    static final int d = 50, T = 100;
    //  n = users, m = items
    static final int n = 943, m = 1682;
    public static float[] userBias = new float[n];
    public static float[] itemBias = new float[m];
    public static float[][] matrixU = new float[n][d];
    public static float[][] matrixV = new float[m][d];
    int recordNum = 0, ratingSum = 0;
    float globalAver;

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
                ratingSum += userIntegers[2];
            }
            //  end of reading file
            
            globalAver = (float)ratingSum / recordNum;
            //  get user bias
            double numeratorUserBias;
            for( Map.Entry<Integer, Map<Integer, Integer>> e : userItems.entrySet()){
                numeratorUserBias = 0.0;
              
                for(Map.Entry<Integer, Integer> ee : e.getValue().entrySet()){
                    numeratorUserBias += Integer.valueOf(ee.getValue()) - globalAver;
                }
                userBias[e.getKey() - 1] = (float) (numeratorUserBias / e.getValue().size());
              
                //  initialize matrix user
                for(int k=0; k<d; ++k){
                    matrixU[e.getKey() - 1][k] = (float) ((float) (Math.random() - 0.5) * 0.01);
                }
            }

            //  get item bias
            double numeratorItemBias;
            for(Map.Entry<Integer, Map<Integer, Integer>> e : itemUsers.entrySet()){
                numeratorItemBias = 0.0;
               
                for(Map.Entry<Integer, Integer> ee : e.getValue().entrySet()){
                    numeratorItemBias += Integer.valueOf(ee.getValue()) - globalAver;
                }
                itemBias[e.getKey() - 1] = (float) (numeratorItemBias / e.getValue().size());
                
                //  initialize matrix item
                for(int k=0; k<d; ++k){
                    matrixV[e.getKey() - 1][k] = (float) ((float) (Math.random() - 0.5) * 0.01);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void doMainJob(String trainFile){
        double gamma = 0.01;
        double alphaU = 0.01, alphaV = 0.01, betaU = 0.01, betaV = 0.01;

        int[] userArr = new int[recordNum];
        int[] itemArr = new int[recordNum];
        int[] ratingArr = new int[recordNum];
        int index = 0;
//        //  get random array
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(new File(trainFile)));
//            String lineString;
//            String[] userStrings;
//            Integer[] userIntegers;
//            try {
//                while((lineString = reader.readLine()) != null){
//                    userStrings = lineString.split("[^0-9]");
//                    userIntegers = new Integer[userStrings.length - 1];
//                    for(int i=0; i<userIntegers.length; ++i){
//                        userIntegers[i] = Integer.valueOf(userStrings[i]);
//                    }
//                    userArr[index] = userIntegers[0];
//                    itemArr[index] = userIntegers[1];
//                    ratingArr[index] = userIntegers[2];
//                    index++;
//                }
//            } catch (IOException e1) {
//                
//                e1.printStackTrace();
//            }
//        } catch (FileNotFoundException e1) {
//            
//            e1.printStackTrace();
//        }
        for( Entry<Integer, Map<Integer, Integer>> e : userItems.entrySet()){
        	for( Entry<Integer, Integer> ee : e.getValue().entrySet()){
        		userArr[index] = e.getKey();
        		itemArr[index] = ee.getKey();
        		ratingArr[index] = ee.getValue();
        		++index;
        	}
        }

        for(int i=0; i<T; ++i){
            for(int j=0; j<recordNum; ++j){

                //  randomly draw a rating record
                int randomNum = (int) (Math.random() * recordNum);
                int randomUser = userArr[randomNum];
                int randomItem = itemArr[randomNum];
                int randomRating = ratingArr[randomNum];

                //  calculate the gradients
                float uv = 0;
                for(int k=0; k<d; ++k){
                    uv += matrixU[randomUser -1][k] * matrixV[randomItem - 1][k];
                }
                double ratingHat = globalAver + userBias[randomUser - 1] + itemBias[randomItem - 1] + uv;
                double deltaGlobalAver = ratingHat - randomRating;
                double deltaUserBias = deltaGlobalAver + betaU * userBias[randomUser - 1] ;
                double deltaItemBias = deltaGlobalAver + betaV * itemBias[randomItem - 1];
                Map<Integer, Double>deltaMatrixU = new HashMap<Integer, Double>(d);
                for(int k=0; k<d; ++k){
                    deltaMatrixU.put(k, deltaGlobalAver * matrixV[randomItem - 1][k] + alphaU * matrixU[randomUser - 1][k]);
                }
                Map<Integer, Double>deltaMatrixV = new HashMap<Integer, Double>(d);
                for(int k=0; k<d; ++k){
                    deltaMatrixV.put(k, deltaGlobalAver * matrixU[randomUser - 1][k] + alphaV * matrixV[randomItem - 1][k]);
                }

                //  update parameters
                globalAver -= gamma * deltaGlobalAver;
                userBias[randomUser - 1] = (float) (userBias[randomUser - 1] - gamma * deltaUserBias);
                itemBias[randomItem - 1] = (float) (itemBias[randomItem - 1] - gamma * deltaItemBias);
                for(int k=0; k<d; ++k){
                    double tempU = matrixU[randomUser - 1][k];
                    double tempV = matrixV[randomItem - 1][k];
                    matrixU[randomUser - 1][k] =  (float) (tempU - gamma * deltaMatrixU.get(k));
                    matrixV[randomItem - 1][k] = (float) (tempV - gamma * deltaMatrixV.get(k));
                }
            }
            gamma *= 0.9;
        }
    }

    void getEvalutions(String testFile){
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File(testFile)));
            String linesString;
            String[] userStrings;
            Integer[] userIntegers;
            int ratingNum = 0;
            double numeratorMAE = 0.0, numeratorRMSE = 0.0;
            double ratingHat;
            try {
                while((linesString = reader.readLine()) != null){
                    userStrings = linesString.split("[^0-9]");
                    userIntegers = new Integer[userStrings.length - 1];
                    for(int i=0; i<userIntegers.length; ++i){
                        userIntegers[i] = Integer.valueOf(userStrings[i]);
                    }
                    double uv = 0.0;
                    for(int i=0; i<d; ++i){
                        double temp1,temp2;
                        if(userItems.containsKey(userIntegers[0])){
                            temp1 = matrixU[userIntegers[0] - 1][i];
                        }
                        else{
                            temp1 = (Math.random() - 0.5) * 0.01;
                        }
                        if(itemUsers.containsKey(userIntegers[1])){
                            temp2 = matrixV[userIntegers[1] - 1][i];
                        }
                        else {
                            temp2 = (Math.random() - 0.05) * 0.01;
                        }
                        uv += temp1 + temp2;
                    }
                    double updateUB, updateIB;
                    if(userItems.containsKey(userIntegers[0])){
                        updateUB = userBias[userIntegers[0] - 1];
                    }
                    else{
                        updateUB = 0;
                    }
                    if(itemUsers.containsKey(userIntegers[1])){
                        updateIB = itemBias[userIntegers[1] - 1];
                    }
                    else {
                        updateIB = 0;
                    }
                    ratingHat = globalAver + updateUB + updateIB + uv;
                    numeratorMAE += Math.abs(userIntegers[2] - ratingHat);
                    numeratorRMSE += Math.pow(userIntegers[2] - ratingHat, 2);
                    ratingNum++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            DecimalFormat df = new DecimalFormat("0.0000");
            System.out.println("MAE: " + df.format(numeratorMAE/ratingNum));
            System.out.println("RMSE: " + df.format(Math.sqrt(numeratorRMSE/ratingNum)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

   
    public static void main(String[] args){
        RSVD test = new RSVD();
        System.out.println("RSVD:");
        System.out.println("d = " + d);
        long startTime = System.currentTimeMillis();
        test.init("u2.base");
        test.doMainJob("u2.base");
        test.getEvalutions("u2.test");
        long endTime = System.currentTimeMillis();
        System.out.println("running time :" + (endTime - startTime)/1000.0 + "s" );
    }
}
