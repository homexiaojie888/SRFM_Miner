package old;
import java.io.*;
import java.util.*;

public class AlgoEMSFUI_B {
    long runtime ;
    long candidatesCount=0;
    long jointCount =0;
    int sfupCount =0;  // the number of SFUP generated
    double maxMemory = 0;     // the maximum memory usage
	long startTimestamp = 0;  // the time the algorithm started
	long endTimestamp = 0;   // the time the algorithm terminated

    String output;
	Map<Integer, Integer> mapItemToTWU;
	BufferedWriter writer = null;  // writer to write the output file

	// this class represent an item and its utility in a transaction
	class Pair{
		int item = 0;
		int utility = 0;
	}
	class ULs{
		List<UtilityList> newPrefixULs;
		List<UtilityList> newExULs;
	}

	public AlgoEMSFUI_B() {
	}


	public void runAlgorithm(String input, int datasize,String output) throws IOException {
		// reset maximum
        this.output=output;
		maxMemory =0;
		startTimestamp = System.currentTimeMillis();
		//  We create a  map to store the TWU of each item
		mapItemToTWU = new HashMap<Integer, Integer>();
		// We scan the database a first time to calculate the TWU of each item.
		BufferedReader myInput = null;
		String thisLine;
		int tid0=1;
		try {
			// prepare the object for reading the file
			myInput = new BufferedReader(new InputStreamReader( new FileInputStream(new File(input))));
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
				}
				if (tid0==datasize){
					break;
				}
				tid0++;
			}
		} catch (Exception e) {
			// catches exception if error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
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

            List<Integer> itemset=new ArrayList<>();
            itemset.add(item);
			UtilityList uList = new UtilityList();
            uList.setItemset(itemset);
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
		int tid =1;
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
				for(Pair pair : revisedTransaction){
					// subtract the utility of this item from the remaining utility
					remainingUtility = remainingUtility - pair.utility;

					// get the utility list of this item
					UtilityList utilityListOfItem = mapItemToUtilityList.get(pair.item);

					// Add a new EMSFUI_B.Element to the utility list of this item corresponding to this transaction
					Element2 element = new Element2(tid, pair.utility, remainingUtility);

					utilityListOfItem.addElement(element);
				}
				if (tid==datasize){
					break;
				}
				tid++; // increase tid number for next transaction

			}

		} catch (Exception e) {
			// to catch error while reading the input file
			e.printStackTrace();
		}finally {
			if(myInput != null){
				myInput.close();
			}
	    }

		// check the memory usage
		checkMemory();

		// Mine the database recursively
		//This array is used to store the max utility value of each frequency,uEmax[0] is meaningless
		//uEmax[1] stored the max utiliey value of all the itemsets which have frequency equals to 1
		int umax[]=new int[tid+1];
		//The list is used to store the current skyline frequent-utility patterns (SFUPs)
		SkylineList SFUA[] = new SkylineList[tid+1];

		//test
		//This method is used to mine all the PSFUPs

		B_Mine(null, listOfUtilityLists, SFUA,  umax);
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
    }

	/**
	 * This is the recursive method to find all potential skyline frequent-utility patterns
	 * @param prefixULs This is the Utility List of the prefix. Initially, it is empty.
	 * @param umax The array of max utility value of each frequency.
	 * @throws IOException
	 */
	private void B_Mine( List<UtilityList> prefixULs, List<UtilityList> exULs, SkylineList SFUA[],  int [] umax)
			throws IOException {
		scan(exULs, SFUA, umax);
		ULs uls = extend(prefixULs, exULs,  umax);
		while(uls.newExULs.size()!=0){
			scan(uls.newExULs, SFUA, umax);
			uls =extend(uls.newPrefixULs, uls.newExULs, umax);
        }
	}

	//generate the candidate SFUIs
	private void scan(List<UtilityList> ULs, SkylineList SFUA[],  int [] umax) throws IOException{
		//breadth-first search
		for(int i=0;i<ULs.size();i++){
			candidatesCount++;
			UtilityList X = ULs.get(i);
			//temp store the frequency of X
			int sup=X.elements.size();
			//judge whether X is a PSFUP
			//if the utility of X equals to the PSFUP which has same frequency with X, insert X to psfupList
			if(X.sumIutils>=umax[sup]){
				judge( X, SFUA, umax);
			}
		}
	}
	//generate the candidate SFUIs by level
	private ULs extend(List<UtilityList> prefixULs, List<UtilityList> exULs,  int [] umax){
		ULs uls = new ULs();
		uls.newExULs = new ArrayList<>();
		uls.newPrefixULs = new ArrayList<>();
		for(int i=0;i<exULs.size();i++){
        	UtilityList pX = exULs.get(i);
        	int sup=pX.elements.size();
        	if(pX.sumIutils + pX.sumRutils >= umax[sup] && umax[sup]!=0){
        		uls.newPrefixULs.add(pX);
				for(int j=i+1; j<exULs.size();j++){
            		UtilityList pY = exULs.get(j);
//                    if (!isSamePredix(pX.itemset,pY.itemset)){
//                        break;
//                    }
                    if (pX.prefixIndex!=pY.prefixIndex){
                        break;

                    }
                    if(prefixULs!=null){
            			UtilityList pXY = construct(prefixULs.get(pY.prefixIndex), pX, pY,umax);
            			if (pXY!=null) {
            				jointCount++;
            				pXY.prefixIndex = uls.newPrefixULs.size()-1;
                			uls.newExULs.add(pXY);
						}
            		}else{
            			UtilityList pXY = construct(null, pX, pY,umax);
            			if(pXY!=null){
            			pXY.prefixIndex = uls.newPrefixULs.size()-1;
            			uls.newExULs.add(pXY);
            			jointCount++;
            		    }
            	    }
        	    }
           }
		}
		return uls;
	}

       boolean isSamePredix(List<Integer> X,List<Integer> Y){
        if (X.size()!=Y.size()){
            return  false;
        }
        for (int i = 0; i < X.size()-1 ; i++) {
            if (!X.get(i).equals(Y.get(i))){
                return  false;
            }
        }
        return true;
        }

	/**
	 * Method to judge whether the PSFUP is a SFUP
	 * @param X The skyline frequent-utility itemset list
	 * @param SFUA The potential skyline frequent-utility itemset list
	 * @param umax The max utility value of each frequency
	 * @throws IOException
	 */
	private void judge(UtilityList X, SkylineList SFUA[], int[] umax) {
		//get the support count of X
		int supCount = X.elements.size();
		/*if(X.sumIutils<=uEmax[supCount+1]){
			break;
		}*/
		if(X.sumIutils==umax[supCount]&&umax[supCount]!=0&&umax[supCount]>umax[supCount+1]){
			if(SFUA[supCount]==null){
                SkylineList skylineList= new SkylineList();
                Skyline temp=new Skyline(X.itemset,supCount,X.sumIutils);
                skylineList.add(temp);
			}else{
                Skyline pattern = new Skyline(X.itemset,supCount,X.sumIutils);
                SFUA[supCount].add(pattern);
				}
			}
			else if(X.sumIutils>umax[supCount]){
            umax[supCount]=X.sumIutils;
            SkylineList skylineList= new SkylineList();
            Skyline temp=new Skyline(X.itemset,supCount,X.sumIutils);
            skylineList.add(temp);
            SFUA[supCount]=skylineList;
		}
		for(int i=1;i<supCount;i++){
            if(X.sumIutils>umax[i]){
                umax[i]=X.sumIutils;
                SFUA[i]=null;
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
	 * Method to write skyline frequent-utility itemset to the output file.
	 * @param skylineList The list of skyline frequent-utility itemsets
	 */
    private void writeOut(SkylineList skylineList[]) throws IOException {


		writer = new BufferedWriter(new FileWriter(output,true));
        StringBuilder buffer = new StringBuilder();
        for(int i=0; i<skylineList.length; i++){
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
		//System.out.println("-----------------------------");
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
    public void printStats(List<Double> runTimelist,List<Double> memorylist,List<Long> candidateslist,List<Long> jointCountlist,List<Integer> patternlist) {

        runTimelist.add((double)runtime/1000);
        memorylist.add(maxMemory);
        candidateslist.add(candidatesCount);
        jointCountlist.add(jointCount);
        patternlist.add(sfupCount);
//        System.out.println("runime(s): "+((double)runtime/1000));
//        System.out.println("memor: "+ maxMemory);
//        System.out.println("candidae: "+candidatesCount);
//        System.out.println("join count: "+jointCount);
//        System.out.println("pattern number: "+sfupCount);

    }
}