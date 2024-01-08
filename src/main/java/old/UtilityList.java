package old;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a UtilityList as used by the HUI-Miner algorithm.
 *
 * @author Jerry Chun-Wei Lin, Lu Yang, Philippe Fournier-Viger
 */

public class UtilityList {
	List<Integer> itemset=new ArrayList<>();  // the item
	int sumIutils = 0;  // the sum of item utilities
	int sumRutils = 0;  // the sum of remaining utilities
	int prefixIndex = 0; //the index of the prefix of an itemset

	//long rentcency=0;



	List<Element2> elements = new ArrayList<Element2>();  // the elements
	
		/**
	 * Method to add an element to this utility list and update the sums at the same time.
	 */
	public void addElement(Element2 element){
		sumIutils += element.iutils;
		sumRutils += element.rutils;
		//rentcency += element.timestamp;
		elements.add(element);
	}
	public int getSupport(){
		return elements.size();
	}

	public List<Integer> getItemset() {
		return itemset;
	}

	public void setItemset(List<Integer> itemset) {
		this.itemset = itemset;
	}

	public int getSumIutils() {
		return sumIutils;
	}

	public void setSumIutils(int sumIutils) {
		this.sumIutils = sumIutils;
	}

	public int getSumRutils() {
		return sumRutils;
	}

	public void setSumRutils(int sumRutils) {
		this.sumRutils = sumRutils;
	}
//	public long getRentcency() {
//		return rentcency;
//	}
//
//	public void setRentcency(long rentcency) {
//		this.rentcency = rentcency;
//	}
}
