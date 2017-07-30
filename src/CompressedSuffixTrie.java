import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


//import test.CompressedSuffixTrie.Node;

public class CompressedSuffixTrie {
	
	private String content;
	private Node root;
	/*  ====================================question_1======================================
	 * In order to construct a compressedSuffixTrie, I need to create Node of this Trie as the helper to store the suffix of children.
	 * And from the big view, I used three loops to achieve this, Outer loop is input the next suffix, and the two inner nested loops are mainly to adjust
	 * and update the suffixNode structure.Therefore the time complexity of it is: O(n^3), note that n = size of input suffix.
	 * 
	 * The Node class need three properties, suffix suffix, arraylist and isLeaf.
	 * Each suffixNode need to maintain an arraylist to store the info of the children.
	 */
	private class Node{
		private String suffix;
		private ArrayList<Node> children = new ArrayList<Node>();
		private boolean isLeaf;
		private int firstIndex;
		
		public Node(){
			this.suffix = "";
			this.firstIndex = -1;
		}
		
		public Node(String suffix){
			this.suffix = suffix;
		}
	}
	
	public CompressedSuffixTrie(String file) throws Exception{
		this.content = read(file);
		String suffixString;
		for(int i = 0; i < this.content.length(); i++){
			if(i == 0){
				this.root = new Node();
			}
			suffixString = this.content.substring(i);
			this.insert(this.root, suffixString, i);
		}	
	}
	/*
	 * When we insert the suffixNode into the CompressedSuffixTrie, there are three cases.
	 * Case1: the input suffix string does not match any child of the currentNode at all.In this case, create 
	 * 		this suffix string as the new Node marked it as isLeaf and append it into the children of the currentNode.
	 * Case2:the suffix string match with the child completely.Also, there are 3 sub cases.
	 * 		sub case1:if the length of suffix string > length of child.suffix,split the suffix string based the overlap
	 * 					create it as a new suffixNode and insert it as the children of this child.
	 * 		sub case2:if the length of suffix string < lenght of child,split the child.suffix based on the overlap, 
	 * 					creat it as new suffixNode, insert it as the children of this child.Also, update the suffix of this child
	 * 					and mark it as isLeaf.
	 * 		sub case3:if the length of suffix string = length of child,check whether it is isLeaf first,if it is not isLeaf, mark it isLeaf
	 * Case3:the suffix string partially match with the child suffix.In this case, we should split suffix string and child's suffix based
	 * 		on the overlap,this is very similar to case2-1.
	 * 
	 */
	private void insert(Node currentNode, String suffixString, int currentIndex){
										
		boolean updateFlag = false;		// Used the update tag to track the status of every operation of insert
		
		for(int i = 0; i < currentNode.children.size(); i++){
			Node child = currentNode.children.get(i);
			
			//Find the min length between suffix of child and suffixString 
			int overlap = Math.min(child.suffix.length(), suffixString.length());
			int count = 0;
			
			// Note that I used count to decide the number of overlap characters 
			// Compare every character from left to right
			for(count = 0; count < overlap; count++){
				if(suffixString.charAt(count) != child.suffix.charAt(count)){
					break;
				}
			}
			//Case1, doesn't match at all
			if(count == 0){
				if(suffixString.charAt(0) < child.suffix.charAt(0)){

					Node suffixNode = new Node(suffixString);
					suffixNode.isLeaf = true;
					suffixNode.firstIndex = currentIndex;
					currentNode.children.add(i, suffixNode);
					updateFlag = true;
					break;
				}else
					continue;	
			}else{

				//Case2, match completely.
				if(count == overlap){
					//case2-1
					if(suffixString.length() > child.suffix.length()){
						if(child.firstIndex > currentIndex)
							child.firstIndex = currentIndex;
						
						String subSuffixString = suffixString.substring(count);
						insert(child, subSuffixString, currentIndex + count);
					
					//case2-2
					}else if(suffixString.length() < child.suffix.length()){
						Node subSuffix = new Node(child.suffix.substring(count));
						subSuffix.isLeaf = child.isLeaf;
						subSuffix.children = child.children;
						subSuffix.firstIndex = child.firstIndex + count;
						
						//update the child
						child.isLeaf = true;
						child.suffix = suffixString;
						
						if(child.firstIndex > currentIndex){
							child.firstIndex = currentIndex;
						}
						
						child.children = new ArrayList<Node>();
						child.children.add(subSuffix);	
						
					//case2-3
					}else{
						if(child.isLeaf){
							System.out.println("Already exist...");
						}else{
							child.isLeaf = true;
							if(child.firstIndex > currentIndex)
								child.firstIndex = currentIndex;
						}
					}
				}else{
					//case3
					//split them based on the overlap
					String subText = child.suffix.substring(count);
					String subSuffixString = suffixString.substring(count);
					
					//create subSuffix suffixNode
					Node subSuffix = new Node(subText);
					subSuffix.isLeaf = child.isLeaf;
					subSuffix.children = child.children;
					subSuffix.firstIndex = child.firstIndex + count;
					
					//update the child suffix
					child.suffix = child.suffix.substring(0, count);
					child.isLeaf = false;
					if(child.firstIndex > currentIndex){
						child.firstIndex = currentIndex;
					}
					
					//mark the suffixNode as isLeaf
					Node suffixNode = new Node(subSuffixString);
					suffixNode.isLeaf = true;
					suffixNode.firstIndex = currentIndex + count;
					
					//update the children of child
					child.children = new ArrayList<Node>();
					
					//Note that should attention lexi-order
					if(subSuffixString.charAt(0) < subText.charAt(0)){
						child.children.add(suffixNode);
						child.children.add(subSuffix);
					}else{
						child.children.add(subSuffix);
						child.children.add(suffixNode);
					}
				}
				updateFlag = true;
				break;
			}
		}
		if(!updateFlag){
			Node suffixNode = new Node(suffixString);
			suffixNode.isLeaf = true;
			suffixNode.firstIndex = currentIndex;
			currentNode.children.add(suffixNode);
		}
	}
	
	/*Read data from file*/
	public static String read(String f) throws IOException{
		File file = new File(f);   //find the destination file
		FileReader fileReader = new FileReader(file); //establish the pipe
		char[] buf = new char[1024 * 8];
		int length = 0;
		String string = "";
		String result = "";
		while((length = fileReader.read(buf)) != -1){
			string += new String(buf, 0, length);
		}
		fileReader.close();  //close input stream
		string = string.trim(); //remove the header and trailer space
		char[] arr = string.toCharArray();
		for(int i = 0; i < arr.length; i++){
			if(arr[i] == 'A' || arr[i] == 'G' || arr[i] == 'C' || arr[i] == 'T')
				result += arr[i];
		}
		return result;
	}
	
	/* ====================================question_2======================================
	 * This findString algorithm reference the lecture note of week10,page24.
	 * And the lecture note said the compressed suffix Tries supports arbitrary pattern matching queries in O(ds)
	 * where s is the size of pattern, d is alphabet size that is "A, G, C, T"
	 * Therefore time complexity is: O(s)
	 */
	public int findString(String pattern){
		// Initialise
		int patternLength = pattern.length();
		int index = 0;
		Node currentNode = this.root;
		boolean tag = false;
		
		while(!tag || currentNode.isLeaf){
			tag = true;
			//Match each of child of the currentNode
			for(int i = 0; i < currentNode.children.size(); i++){
				Node child = currentNode.children.get(i);
				//record the startIndex of child
				int childIndex = child.firstIndex;
				
				//Match every character between pattern and child.
				if(pattern.charAt(index) == child.suffix.charAt(0)){
					int childLength = child.suffix.length();
					//if suffix is short than or same length of child
					if(patternLength <= childLength){
						//Match completely,success
						if(pattern.substring(index, index + patternLength).equals(child.suffix.substring(0,patternLength))){
							return childIndex - index;
						}else{
							return -1;
						}
					//if suffix is longer than the child
					}else{
						if(pattern.substring(index, index + childLength).equals(child.suffix.substring(0, childLength))){
							//update suffix length 
							patternLength = patternLength - childLength;
							//update suffix index
							index += childLength;
							// process the children of this child
							currentNode = child;
							tag = false;
							break;
						}
					}
				}
			}
		}
		return -1;
	}
		
	/* ====================================question_3======================================
	 * In order to get the LCS bewteen two strings,I take some ideas from LCS algorithm from textbook and lecture note.
	 * 
	 * First: 
	 * 		Read the sequence from files
	 * Second:
	 * 		I compute the LCS based on string1 and string2.It is dominated by two nested for loops
	 * 		with the outer one iterating n times and inner m times.Since the assginment inside the loop
	 * 		each require O(1), therefore the time complexity is O(m * n).Note that I used the flag and 
	 * 		other index to continue from the break point.
	 * Third:
	 * 		Write the LCS string into file3 and compute the similarity.The time complexity, O(1)
	 */
	public static float similarityAnalyser(String f1, String f2, String f3) throws IOException{
		
		//read the sequence from file1, file2
		String text1 = read(f1);
		String text2 = read(f2);
		
		//find the LCS between two strings.
		String LCS = LCS(text1, text2);
	
		// write the lcs into file3
		File file = new File(f3);
		FileWriter fileWriter = new FileWriter(file);
		fileWriter.write(LCS);
		fileWriter.close();
		
		//compute the similarity 
		float text1_length = text1.length();
		float text2_length = text2.length();
		float LCS_length = LCS.length();
		
		float similarity = LCS_length / (text1_length > text2_length ? text1_length : text2_length);
		
		return similarity;
	}
	
		
	/* Find the LCS between two strings*/
	//Note that there are two nested loop, the time complexity is O(mn)
	public static String LCS(String str1, String str2){
		
		//record length of str1, and str2
		int m = str1.length();
		int n = str2.length();
		String result = "";
		boolean flag ;
		int index = 0, j = 0;
		
		for(int i = 0; i < m; i++){
			flag = true;
			//record the point
			index = j;
			for(; j < n; j++){
				// if match, j++ and break, mark flag as false.
				if(str1.charAt(i) == str2.charAt(j)){
					result += str1.charAt(i);
					j++;
					flag = false;
					break;
				}
			}
			//continue from the point
			if(flag){
				j = index;
			}
		}
		return result;
	}
	
	
}
