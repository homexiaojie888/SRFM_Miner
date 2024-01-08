package old;

import java.io.*;
import java.util.*;

public class AlgoMineEMSFUI_D{
	BufferedWriter Testwriter;
	long runtime ;
	long candidatesCount=0;
	long jointCount =0;
	int sfupCount =0;  // the number of SFUP generated
	double maxMemory = 0;     // the maximum memory usage
	long startTimestamp = 0;  // the time the algorithm started
	long endTimestamp = 0;   // the time the algorithm terminated
	String output;
	int tid =0;
	/** the number of utility-list that was constructed */

	Map<Integer, int[]> ISU_1;
	Map<Integer,Integer> mapItemToTWU;

	// The ISU_2 structure:  key: item   key: another item   value: twu and support */
	Map<Integer, Map<Integer, int[]>> ISU_2;
	BufferedWriter writer = null;  // writer to write the output file

	// this class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
	}

	public AlgoMineEMSFUI_D() {
	}

	public void runAlgorithm(String input, int datasize, String output) throws IOException {
		this.output=output;
		// reset maximum
		maxMemory =0;
		startTimestamp = System.currentTimeMillis();

		//  We create a  map to store the TWU of each item
		ISU_1 = new HashMap<>();
		mapItemToTWU = new HashMap<>();
		ISU_2 = new HashMap<>();
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(input)));
			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}
				// split the transaction according to the : separator
				String split[] = thisLine.split(":");
				// the first part is the list of items
				String items[] = split[0].split(" ");
				//the third part is the utilities of items
				String itemUtils[] = split[2].split(" ");
				// the second part is the transaction utility
				int transactionUtility = Integer.parseInt(split[1]);
				// for each item, we add the transaction utility to its TWU
				for(int i=0; i <items.length; i++){
					// convert item to integer
					Integer item = Integer.parseInt(items[i]);
					// get the current TWU of that item
					Integer twu = mapItemToTWU.get(item);
					// add the utility of the item in the current transaction to its twu
					twu = (twu == null)?
							transactionUtility : twu + transactionUtility;
					mapItemToTWU.put(item, twu);
					// add the utility of the item in the current transaction to its twu
					int[] itemInfo = ISU_1.get(item);
					if(itemInfo==null){
						itemInfo = new int[2];
						itemInfo[0] = Integer.parseInt(itemUtils[i]);
						itemInfo[1] = 1;
						ISU_1.put(item, itemInfo);
					}else{
						itemInfo[0] += Integer.parseInt(itemUtils[i]);
						itemInfo[1] += 1;
						ISU_1.put(item, itemInfo);
					}
				}
				tid++; // increase tid number for next transaction
				if (tid==datasize){
					break;
				}
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		int[] umax =new int[tid+1];
		//update umax by ISU-1
		for(Map.Entry<Integer, int[]> entry: ISU_1.entrySet()){
			int itemSup = entry.getValue()[1];
			int itemUtil = entry.getValue()[0];
			if(itemUtil>=umax[itemSup])
				umax[itemSup]=itemUtil;
		}
		//update umax by itself
		for(int i=2; i<umax.length;i++){
			for(int j=1; j<i; j++){
				if(umax[j]<umax[i]){
					umax[j]=umax[i];
				}
			}
		}

		// CREATE A LIST TO STORE THE UTILITY LIST OF ITEMS
		List<UtilityList> listOfUtilityLists = new ArrayList<UtilityList>();
		// CREATE A MAP TO STORE THE UTILITY LIST FOR EACH ITEM.
		// Key : item    Value :  utility list associated to that item
		Map<Integer, UtilityList> mapItemToUtilityList = new HashMap<Integer, UtilityList>();

		// For each item
		for(Integer item: mapItemToTWU.keySet()){
			// create an empty Utility List that we will fill later.
			UtilityList uList = new UtilityList();
			uList.itemset.add(item);
			mapItemToUtilityList.put(item, uList);
			// add the item to the list of high TWU items
			listOfUtilityLists.add(uList);
		}

		// SORT THE LIST OF HIGH TWU ITEMS IN ASCENDING ORDER
		Collections.sort(listOfUtilityLists, new Comparator<UtilityList>(){
			public int compare(UtilityList o1, UtilityList o2) {
				// compare the TWU of the items
				return compareItems(o1.itemset.get(o1.itemset.size()-1), o2.itemset.get(o2.itemset.size()-1));
			}
			} );

		// SECOND DATABASE PASS TO CONSTRUCT THE UTILITY LISTS OF ALL 1-ITEMSETS
		// variable to count the number of transaction
		int tid2=0;
		try {
			// prepare object for reading the file
			myInput = new BufferedReader(new InputStreamReader(new FileInputStream(new File(input))));

			// for each line (transaction) until the end of file
			while ((thisLine = myInput.readLine()) != null) {
				// if the line is  a comment, is  empty or is a
				// kind of metadata
				if (thisLine.isEmpty() == true ||
						thisLine.charAt(0) == '#' || thisLine.charAt(0) == '%'
								|| thisLine.charAt(0) == '@') {
					continue;
				}

				tid2++;
				// split the line according to the separator
				String split[] = thisLine.split(":");
				// get the list of items
				String items[] = split[0].split(" ");
				// get the list of utility values corresponding to each item
				// for that transaction
				String utilityValues[] = split[2].split(" ");

				// Copy the transaction into lists

				int remainingUtility =0;

				// Create a list to store items
				List<Pair> revisedTransaction = new ArrayList<Pair>();
				// for each item
				for(int i=0; i <items.length; i++){
					/// convert values to integers
					Pair pair = new Pair();
					pair.item = Integer.parseInt(items[i]);
					pair.utility = Integer.parseInt(utilityValues[i]);
					// add it
					revisedTransaction.add(pair);
					remainingUtility += pair.utility;
				}

				Collections.sort(revisedTransaction, new Comparator<Pair>(){
					public int compare(Pair o1, Pair o2) {
						return compareItems(o1.item, o2.item);
					}});
				// for each item left in the transaction
				for(int i=0; i<revisedTransaction.size(); i++){
					Pair pair =  revisedTransaction.get(i);
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;
					// get the utility list of this item
					UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);
					// Add a new Element to the utility list of this item corresponding to this transaction
					Element2 element = new Element2(tid2, pair.utility, remainingUtility);
					utilityListOfItem.addElement(element);

					//generate ISU_2
					Map<Integer, int[]> mapISU_2 = ISU_2.get(pair.item);
					if(mapISU_2 == null) {
						mapISU_2 = new HashMap<>();
						ISU_2.put(pair.item, mapISU_2);
					}
					for(int j = i+1; j< revisedTransaction.size(); j++){
						Pair pairAfter = revisedTransaction.get(j);
						int[] info = mapISU_2.get(pairAfter.item);
						if(info==null){
							info = new int[2];
							info[0]=pair.utility+pairAfter.utility;
							info[1]=1;
							mapISU_2.put(pairAfter.item, info);
						}else{
						    info[0] = info[0]+pair.utility+pairAfter.utility;
							info[1] = info[1]+1;
							mapISU_2.put(pairAfter.item, info);
						}
						ISU_2.put(pair.item,mapISU_2);
					}
				}
			if (tid2==datasize){
				break;
			}

			}

		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }
		//System.out.println("database size��"+tid+" "+tid2);

		// check the memory usage
		checkMemory();

		// Mine the database recursively
		//This array is used to store the max utility value of each frequency,umax[0] is meaningless
		//umax[1] stored the max utiliey value of all the itemsets which have frequency equals to 1

		//The list is used to store the current skyline frequent-utility patterns (SFUPs)
		SkylineList[] SFUA = new SkylineList[tid+1];
		//update operation by ISU-2
		for(Map.Entry<Integer, Map<Integer, int[]>> entryFirstItem: ISU_2.entrySet()){
			for(Map.Entry<Integer,int[]> entrySecondItem: entryFirstItem.getValue().entrySet()){
				if(entrySecondItem.getValue()[0]>umax[entrySecondItem.getValue()[1]])
					umax[entrySecondItem.getValue()[1]]=entrySecondItem.getValue()[0];
			}
		}
		//update umax by itself
		for(int i=2; i<umax.length;i++){
			for(int j=1; j<i; j++){
				if(umax[j]<umax[i]){
					umax[j]=umax[i];
				}
			}
		}
		//test
		//This method is used to mine all the PSFUPs
//		Testwriter = new BufferedWriter(new FileWriter(".//test_"+datasize));
		D_Mine(null, listOfUtilityLists, SFUA,  umax);
//		Testwriter.flush();
		//This method is used to write out all the PSFUPs
		writeOut(SFUA);

		// check the memory usage again and close the file.
		checkMemory();
		// close output file
		writer.close();
		// record end time
		endTimestamp = System.currentTimeMillis();
		runtime=endTimestamp-startTimestamp;
	}


	private int compareItems(int item1, int item2) {
		return  item1-item2;
//		int compare = mapItemToTWU.get(item1) - mapItemToTWU.get(item2);
//		// if the same, use the lexical order otherwise use the TWU
//		return (compare == 0)? item1 - item2 :  compare;
	}

	/**
	 * This is the recursive method to find all potential skyline frequent-utility patterns
	 * @param pUL This is the Utility List of the prefix. Initially, it is empty.
	 * @param ULs The utility lists corresponding to each extension of the prefix.
	 * @param umax The array of max utility value of each frequency.Initially, it is zero.
	 * @throws IOException
	 */
	private void D_Mine(UtilityList pUL, List<UtilityList> ULs, SkylineList[] SFUA, int [] umax)
			throws IOException {
		// For each extension X of prefix P
		for(int i=0; i< ULs.size(); i++){
			candidatesCount++;

			UtilityList X = ULs.get(i);
//			if (X.itemset.size()==1&&X.itemset.get(0)==20685){
//				System.out.print("");
//			}
//			Testwriter.write(X.itemset.toString());
//			Testwriter.newLine();
			//temp store the frequency of X
			int supCount=X.getSupport();
			//judge whether X is a PSFUP
			//if the utility of X equals to the PSFUP which has same frequency with X, insert X to psfupList
			if(X.sumIutils>=umax[supCount]){
				judge(X, SFUA, umax);
			}
			// If the sum of the remaining utilities for pX
			// is higher than uEmax[j], we explore extensions of pX.
			// (this is the pruning condition)&&
			// umax[supCount]!=0
			if(((X.sumIutils + X.sumRutils) >= umax[supCount]) ){
				// This list will contain the utility lists of pX extensions.
				List<UtilityList> exULs = new ArrayList<UtilityList>();
				// For each extension of p appearing
				// after X according to the ascending order
				for(int j=i+1; j < ULs.size(); j++){
					UtilityList Y = ULs.get(j);
					// we construct the extension pXY
					// and add it to the list of extensions of pX
					UtilityList Pxy = construct(pUL, X, Y, umax);
					//Hup-Miner
					if(Pxy != null&& !Pxy.elements.isEmpty()){
						exULs.add(Pxy);
						jointCount++;
					}
				}
				// We make a recursive call to discover all itemsets with the prefix pXY
				D_Mine(X, exULs, SFUA, umax);
			}
		}
	}

	/**
	 * Method to judge whether the PSFUP is a SFUP
	 * @param umax The max utility value of each frequency
	 * @throws IOException
	 */
	private void judge(UtilityList X, SkylineList[] SFUA, int[] umax) throws IOException {
		//get the support count of X
		int supCount = X.getSupport();
		/*if(X.sumIutils<=uEmax[supCount+1]){
			break;
		}*/
		//System.out.println(supCount);
		if(X.sumIutils==umax[supCount]&&(supCount==umax.length-1||umax[supCount]>umax[supCount+1])){
			if(SFUA[supCount]!=null){
				Skyline pattern = new Skyline(X.itemset,supCount,X.sumIutils);
				SFUA[supCount].add(pattern);
			}else{
				SkylineList skylineList= new SkylineList();
				Skyline temp=new Skyline(X.itemset,supCount,X.sumIutils);
				skylineList.add(temp);
				SFUA[supCount]=skylineList;
			}
		}
		else if(X.sumIutils>umax[supCount]){
			umax[supCount]=X.sumIutils;
			SkylineList skylineList= new SkylineList();
			Skyline temp=new Skyline(X.itemset,supCount,X.sumIutils);
			skylineList.add(temp);
			SFUA[supCount]=skylineList;

			for(int i=1;i<supCount;i++){
				if(X.sumIutils>umax[i]){
					umax[i]=X.sumIutils;
					SFUA[i]=null;
				}
			}
		}
	}

	/**
	 * This method constructs the utility list of pXY
	 * @param P :  the utility list of prefix P.
	 * @param px : the utility list of pX
	 * @param py : the utility list of pY
	 * @return the utility list of pXY
	 */
	private UtilityList construct(UtilityList P, UtilityList px, UtilityList py, int[] umax) {
		// create an empy utility list for pXY
		UtilityList pxyUL = new UtilityList();
		pxyUL.itemset.addAll(px.itemset);
		pxyUL.itemset.add(py.itemset.get(py.itemset.size()-1));
		//the novel pruning strategy
		int totalUtility = px.sumIutils+px.sumRutils;
		int totalSup = px.getSupport();
		// for each element in the utility list of pX
		List<Element2> elementListPx=px.elements;
		List<Element2> elementListPy=py.elements;
		for (int i = 0,j = 0; i < elementListPx.size()&&j < elementListPy.size(); ) {
            Element2 ex=elementListPx.get(i);
            Element2 ey=elementListPy.get(j);
			if (ex.tid==ey.tid){

				if(P == null){

                    Element2 newElement = new Element2(ex.tid, ex.iutils + ey.iutils, ey.rutils);
					pxyUL.addElement(newElement);

				}else{

                    Element2 e = findElementWithTID(P, ex.tid);
					if(e != null){

                        Element2 newElement = new Element2(ex.tid, ex.iutils + ey.iutils - e.iutils, ey.rutils);
						pxyUL.addElement(newElement);

					}else {

                        Element2 newElement = new Element2(ex.tid,ex.iutils + ey.iutils, ey.rutils);
						pxyUL.addElement(newElement);

					}
				}

				i++;j++;
			}else if (ex.tid>ey.tid){
				j++;
			}else if (ex.tid<ey.tid){
				//== new optimization - LA-prune == /
				totalUtility -= (ex.iutils+ex.rutils);
				totalSup-=1;
				if(totalUtility<umax[totalSup]){
					return null;
				}

				i++;
			}

		}
		// return the utility list of pXY.
		return pxyUL;
	}

	/**
	 * Do a binary search to find the element with a given tid in a utility list
	 * @param ulist the utility list
	 * @param tid  the tid
	 * @return  the element or null if none has the tid.
	 */
	private Element2 findElementWithTID(UtilityList ulist, int tid){
		List<Element2> list = ulist.elements;
		// perform a binary search to check if  the subset appears in  level k-1.
        int first = 0;
        int last = list.size() - 1;
        // the binary search
        while( first <= last )
        {
        	int middle = ( first + last ) >>> 1; // divide by 2

            if(list.get(middle).tid < tid){
            	first = middle + 1;  //  the itemset compared is larger than the subset according to the lexical order
            }
            else if(list.get(middle).tid > tid){
            	last = middle - 1; //  the itemset compared is smaller than the subset  is smaller according to the lexical order
            }
            else{
            	return list.get(middle);
            }
        }
		return null;
	}

	/**
	 * Method to write out itemset name
	 * @param prefix This is the current prefix
	 * @param item This is the new item added after the prefix
	 * @return  the itemset name
	 */
	private String itemSetString(int[] prefix, int item) throws IOException {
		//Create a string buffer
		StringBuilder buffer = new StringBuilder();
		// append the prefix
		for (int i = 0; i < prefix.length; i++) {
			buffer.append(prefix[i]);
			buffer.append(' ');
		}
		// append the last item
		buffer.append(item);
		return buffer.toString();
	}

	public void printStats(List<Double> runTimelist,List<Double> memorylist,List<Long> candidateslist,List<Long> jointCountlist,List<Integer> patternlist) {

		runTimelist.add((double)runtime/1000);
		memorylist.add(maxMemory);
		candidateslist.add(candidatesCount);
		jointCountlist.add(jointCount);
		patternlist.add(sfupCount);
//		System.out.println("runime(s): "+((double)runtime/1000));
//		System.out.println("memory: "+ maxMemory);
//		System.out.println("candidae: "+candidatesCount);
//		System.out.println("join count: "+jointCount);
//		System.out.println("pattern number: "+sfupCount);

	}
	/**
	 * Method to write skyline frequent-utility itemset to the output file.
	 * @param skylineList The list of skyline frequent-utility itemsets
	 */
	private void writeOut(SkylineList skylineList[]) throws IOException {
		//Create a string buffer
		writer = new BufferedWriter(new FileWriter(output,true));
		StringBuilder buffer = new StringBuilder();
		for(int i=1; i<skylineList.length; i++){
			if(skylineList[i]!=null){
				for(int j=0; j<skylineList[i].size(); j++){

					buffer.append(skylineList[i].get(j).itemSet);
					buffer.append(" #SUP:");
					buffer.append(skylineList[i].get(j).frequent);
					buffer.append(" #UTILITY:");
					buffer.append(skylineList[i].get(j).utility);
					buffer.append(System.lineSeparator());
					// write to file
					//��¼skylineģʽ�ĸ���
					sfupCount=sfupCount+1;
				}
			}
		}
		writer.write(buffer.toString());
		//System.out.print(buffer.toString());
		writer.write("----------------------------");
		//System.out.println("----------------------------");
		writer.write("\n");
		writer.flush();
		writer.close();
	}


	/**
	 * Method to check the memory usage and keep the maximum memory usage.
	 */
	private void checkMemory() {
		// get the current memory usage
		double currentMemory = (Runtime.getRuntime().totalMemory() -  Runtime.getRuntime().freeMemory())
				/ 1024d / 1024d;
		// if higher than the maximum until now
		if (currentMemory > maxMemory) {
			// replace the maximum with the current memory usage
			maxMemory = currentMemory;
		}
	}

	/**
	 * Print statistics about the latest execution to System.out.
	 */
	public void printStats() {
		System.out.println("=============  uEmax skyline ALGORITHM v 2.11 - STATS =============");
		System.out.println(" Total time ~ " + (endTimestamp - startTimestamp) + " ms");
		System.out.println(" Memory ~ " + maxMemory+ " MB");
		System.out.println(" old.Skyline itemsets count : " + sfupCount);
		System.out.println(" Join itemsets count : " + jointCount);
		System.out.println("===================================================");
	}
}