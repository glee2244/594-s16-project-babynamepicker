package babynamepicker;
/**
 * This class represents the DataSet of the Baby Name Picker project.
 */


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Stack;

public class Dataset {
	/**
	 * instance variables
	 */
	private HashMap<String, BabyName> dataMap;
	private ArrayList<BabyName> dataList;
	private ArrayList<String> filteredList;
	private char prevInitial, currentInitial;
	private String prevGender, prevSort, currentGender, currentSort;
	private int prevNYears, prevNumSuggest, currentNYears, currentNumSuggest;
	private Stack<BabyName> sortingStack;
	private User user1;
	private User user2;
	private int currentUser;
	
	/**
	 * constructor
	 */
	public Dataset() {
		dataMap = new HashMap<String, BabyName>();
		dataList = new ArrayList<BabyName>();
		filteredList = new ArrayList<String>();
		prevGender = "A";
		prevSort = "";
		prevInitial = '0';
		prevNYears = -1;
		prevNumSuggest = -1;
		currentGender = "A";
		currentSort = "A";
		currentInitial = '0';
		currentNYears = 0;
		currentNumSuggest = 0;
		sortingStack = new Stack<BabyName>();
		
		user1 = new User(1);
		user2 = new User(2);
	}
	
	
	/**
	 * get baby name by the name string
	 * @param name
	 * @return the BabyName object
	 */
	public BabyName getBabyName(String name) {
		return dataMap.get(name);
	}
	
	
	/**
	 * @return the dataList
	 */
	public ArrayList<BabyName> getDataList() {
		return dataList;
	}


	/**
	 * Add new name to the data HashMap
	 * @param name
	 * @param sex
	 * @param initial
	 * @param year
	 * @param pop, popularity of that name (frequency) in that year 
	 */
	public void addName(String name, String sex, char initial, int year, int pop) {
		//add new BabyName object if the name does not already exist in the map
		if(!dataMap.containsKey(name)) {
			dataMap.put(name, new BabyName(name, sex, initial));
		}
		//if the name already exists but its sex varies, set it to be unisex
		if(dataMap.containsKey(name) && !dataMap.get(name).getSex().equals(sex)) {
			dataMap.get(name).setSex("U");
		}
		//add year and popularity to the BabyName's popularity HashMap
		dataMap.get(name).addPop(year, pop);
	}
	
	
	/**
	 * After parsing all baby names from files, make the Final List
	 * (copy all baby names in HashMap into an ArrayList)
	 * Since the keySet of a HashMap cannot be sorted,
	 * an ArrayList version of the data is needed for various sorting purposes
	 */
	public void finalList() {
		for(String i : dataMap.keySet()) {
			//for the years that this name didn't appear, set the popularity for that year to 0
			for(int j = 1880; j < 2015; j++) {
				if(!dataMap.get(i).containsYear(j)) {
					dataMap.get(i).addPop(j, 0);
				}
			}
			dataList.add(dataMap.get(i));
		}
		//by default, have it sorted alphabetically
		Collections.sort(dataList, new nameComparator());
	}
	

	/**
	* clear dataList
	*/
	public void clearList() {
		dataList.clear();
	}
	

	/**
	* filter dataList according to the filters
	*/
	public void filterList() {
		/*
		 * gender: A= show all; F= female; M= male; U= unisex
		 * sort: A= alphabetical; P= pop high to low; p= pop low to high
		 * initial: 0= no preference
		 * nYears: 0= all time
		 * numSuggest: 0= show all
		 */
		 
		//re-filter the list ONLY if any filter has changed from before
		if(!(currentGender.equals(prevGender) && currentSort.equals(prevSort) && currentInitial == prevInitial
				&& currentNYears == prevNYears && currentNumSuggest == prevNumSuggest)) {
			 
			//clear the list
			filteredList.clear();
			 
			//if the filter value for "most popular in the last n years" has changed, recalculate popTotal
			if(currentNYears != prevNYears) {
				for(BabyName i : dataList) {
					if(currentNYears == 0) i.setPopTotal(135);
					else i.setPopTotal(currentNYears);
				}
			}
			 
			//if sorting pref has changed, or most pop in n years has changed
			if(!currentSort.equals(prevSort) || currentNYears != prevNYears){

				//sort alphabetically
				if(currentSort.equals("A")) {
					Collections.sort(dataList, new nameComparator());
		
				 } else {
					//else sort by popularity
					if(currentNYears != prevNYears || currentSort.equals("P") || ((prevSort.equals("A") || prevSort.equals("")) && currentSort.equals("p") )) {
						Collections.sort(dataList, new popComparator());
					}
					 
					//if sorting low to high, use a stack to reverse the order
					if(currentSort.equals("p")) {
						sortingStack.clear();
						//push BabyNames onto stack
						for(int i = 0; i < dataList.size(); i++) {
							sortingStack.push(dataList.get(i));
						}
						//add to list the name string of the popped off BabyName object
						dataList.clear();
						while(!sortingStack.isEmpty()) {
							dataList.add(sortingStack.pop());
						}
					}
				}
			}
			 
			//set how many to add to the list
			int n, i, j;
			if(currentNumSuggest == 0) {
				n = dataList.size(); //add all
			} else {
				n = currentNumSuggest;
			}
			i = 0;
			j = 0;
			 
			//add names to list!
			while(j < n) {
				BabyName b = dataList.get(i);
				//add only names of the specified gender
				if(currentGender.equals("A") || (b.getSex().equals(currentGender) )) {
					//if a certain initial is preferred
					if(currentInitial != '0') {
						if(b.getInitial() == currentInitial) { //add only names of that initial
							String s = b.getName() + " (" + b.getPopTotal() + " " + b.getSex() + ")";
							filteredList.add(s);
						}
					} else {
						String s = b.getName() + " (" + b.getPopTotal() + " " + b.getSex() + ")";
						filteredList.add(s);
					}
				}

				i++;
				if(currentNumSuggest == 0) j = i;
				else j = filteredList.size();
			}
			 
			//store these filters to compare with the next batch
			prevGender = currentGender;
			prevSort = currentSort;
			prevInitial = currentInitial;
			prevNYears = currentNYears;
			prevNumSuggest = currentNumSuggest;
		}
		 
	}
	

	/**
	* reset the filters
	*/
	public void resetFilters() {
		prevGender = "A";
		prevSort = "";
		prevInitial = '0';
		prevNYears = -1;
		prevNumSuggest = -1;
		currentGender = "A";
		currentSort = "A";
		currentInitial = '0';
		currentNYears = 0;
		currentNumSuggest = 0;
	}


	/**
	 * to sort names alphabetically
	 */
	public class nameComparator implements Comparator<BabyName>{
		public int compare(BabyName b1, BabyName b2) {
			String baby1 = b1.getName();
			String baby2 = b2.getName();
					
			return baby1.compareTo(baby2);
		}
	}
	 
	/**
	 * to sort names by rating
	 */
	public class ratingComparator implements Comparator<BabyName> {
		public int compare(BabyName o1, BabyName o2) {
			double comp = 0;
			comp = o2.getFinalRating() - o1.getFinalRating();
			comp = o2.getPopTotal() - o1.getPopTotal();
					
			if(comp > 0) return 1;
			else if(comp < 0) return -1;
			else return 0;
		}	
	}
		
	/**
	 * sort names by popularity
	 */
	public class popComparator implements Comparator<BabyName>  {
		public int compare(BabyName o1, BabyName o2) {
			double comp = 0;
			comp = o2.getPopTotal() - o1.getPopTotal();
					
			if(comp > 0) return 1;
			else if(comp < 0) return -1;
			else return 0;
		}	
	}


	/**
	 * @return the filteredList
	 */
	public ArrayList<String> getFilteredList() {
		return filteredList;
	}

	/**
	 * @param currentInitial the currentInitial to set
	 */
	public void setCurrentInitial(char currentInitial) {
		this.currentInitial = currentInitial;
	}

	/**
	 * @param currentGender the currentGender to set
	 */
	public void setCurrentGender(String currentGender) {
		this.currentGender = currentGender;
	}

	/**
	 * @param currentSort the currentSort to set
	 */
	public void setCurrentSort(String currentSort) {
		this.currentSort = currentSort;
	}

	/**
	 * @param currentNYears the currentNYears to set
	 */
	public void setCurrentNYears(int currentNYears) {
		this.currentNYears = currentNYears;
	}

	/**
	 * @param currentNumSuggest the currentNumSuggest to set
	 */
	public void setCurrentNumSuggest(int currentNumSuggest) {
		this.currentNumSuggest = currentNumSuggest;
	}
	
	/**
	 * Converts an ArrayList of Strings of babies' names to ArrayList of corresponding BabyName objects
	 * @param inputList arraylist of strings which are the babies' names
	 * @return arraylist of corresponding BabyName objects
	 */
	public ArrayList<BabyName> convertNameToBabyNameList(ArrayList<String> inputList) {
		ArrayList<BabyName> convertedList = new ArrayList<BabyName>();
		
		for (String s : inputList) {
			String token = s.substring(0, s.indexOf(' '));
			convertedList.add(getBabyName(token));
		}
		
		return convertedList;
	}
	
	/**
	 * Sets the filtered list for a user.
	 * @param user user who this filtered list is associated with
	 * @param inputList filtered list to set for specified user
	 */
	public void setUserNameList(int user, ArrayList<BabyName> inputList) {
		
		if (user == 1) {
			user1.setNameList(inputList);
		} else if (user == 2) {
			user2.setNameList(inputList);
		}
	}
	
	/**
	 * Get the filtered list for a specified user
	 * @param user user
	 * @return filtered list for this specified user
	 */
	public ArrayList<BabyName> getUserNameList(int user) {
		if (user == 1) {
			return user1.getNameList();
		} else if (user == 2) {
			return user2.getNameList();
		}
		return null;
	}
	
	/**
	 * Sets the current user 
	 * @param n value of the current user, either 1 or 2
	 */
	public void setCurrentUser(int n) {
		currentUser = n;
	}
	
	/**
	 * Get the value of the current user
	 * @return value of the current user
	 */
	public int getCurrentUser() {
		return currentUser;
	}
}
