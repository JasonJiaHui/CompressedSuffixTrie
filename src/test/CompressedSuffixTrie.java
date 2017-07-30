package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CompressedSuffixTrie {

	private String line;
	private SuffixTrieNode root;

	//class SuffixTrieNode is a helper class for building CompressedSuffixTrie
	private class SuffixTrieNode {

		//except save the text, every node also save the index, in order to matching convenient
		private String text;
		private List<SuffixTrieNode> children = new ArrayList<SuffixTrieNode>();
		private boolean leaf;
		private int firstStartIndex;

		//no argument constructor used to initialize the root for tire
		public SuffixTrieNode() {
			this.text = "";
			firstStartIndex = -1;
		}

		
		public SuffixTrieNode(String test) {
			this.text = test;
		}
	}

	//the constructor of CompressedSuffixTrie:
	//read the string from file and for every suffix invokes the addNode method to build the trie by traversal
	//for the assNode method there are two nested loops and in general for the worst condition the constructor will run n^3 times,
	//therefore, for this constructor method the time complexity is O(n^3).
	public CompressedSuffixTrie(String filePath) throws Exception {
		this.line = ReadFile(filePath + ".txt");
		String suffixString;
		for(int i=0;i<line.length();i++){
			if(i==0)
				this.root = new SuffixTrieNode();
			suffixString = line.substring(i);
			this.addNode(this.root,suffixString,i);
		}
	}

	//for this method, it used to compare every suffix(here called key) with tree's current children
	//and based on the different situations do the proper process
	//there are three conditions when add children for tree:
	//1:child doesn't match any character with the new key when count = 0
	//2:child partially match any character with the new key when 0<count<len
	//3:child totally match any character with the new key when count=len
	private void addNode(SuffixTrieNode pointNode, String key, int startIndex) throws Exception {
		boolean flag = false;

		for (int i = 0; i < pointNode.children.size(); i++) {
			SuffixTrieNode child = pointNode.children.get(i);

			//use min(child.text.length, key.length)
			int len = child.text.length() < key.length() ? child.text.length() : key.length();
			int count = 0;
			for (; count < len; count++) {
				if (key.charAt(count) != child.text.charAt(count)) {
					break;
				}
			}
			
			//this child doesn't match any character with the new key
			if (count == 0) {
				//order keys by lexi-order
				if (key.charAt(0) < child.text.charAt(0)) {
					SuffixTrieNode node = new SuffixTrieNode(key);
					node.leaf = true;
					node.firstStartIndex = startIndex;
					pointNode.children.add(i, node);
					flag = true;
					break;
				} else   //key.charAt(0)>child.text.charAt(0)
					continue;

			} else {
				//current child's text totally matches with the new key; count=len 
				if (count == len) {
					if (key.length() == child.text.length()) {
						if (child.leaf)
							throw new Exception("The string has already been duplicated ");
						else {
							child.leaf = true;
							if (child.firstStartIndex > startIndex)
								child.firstStartIndex = startIndex;
						}
					}
					if (key.length() > child.text.length()) {
						if (child.firstStartIndex > startIndex)
							child.firstStartIndex = startIndex;
						String subKey = key.substring(count);
						addNode(child, subKey, startIndex + count);					
					}
					if (key.length() < child.text.length()) {
						SuffixTrieNode geneSubText = new SuffixTrieNode(child.text.substring(count));
						geneSubText.leaf = child.leaf;
						geneSubText.children = child.children;
						geneSubText.firstStartIndex = child.firstStartIndex + count;
						child.leaf = true;
						child.text = key;
						if (child.firstStartIndex > startIndex)
							child.firstStartIndex = startIndex;
						child.children = new ArrayList<SuffixTrieNode>();
						child.children.add(geneSubText);
					}
				//current child's text partially matches with the new key; 0<count<len 
				} else {
					//split at count
                    String subText = child.text.substring(count);
					String subKey = key.substring(count);

					SuffixTrieNode geneSubText = new SuffixTrieNode(subText);
					geneSubText.leaf = child.leaf;
					//inherited from parent
					geneSubText.children = child.children;
					geneSubText.firstStartIndex = child.firstStartIndex + count;

					//update child's text
					child.text = child.text.substring(0, count);
					if (child.firstStartIndex > startIndex)
						child.firstStartIndex = startIndex;
					//child is not terminal now due to split, it is inherited by subChildNode
					child.leaf = false;

					SuffixTrieNode node = new SuffixTrieNode(subKey);
					node.leaf = true;
					node.firstStartIndex = startIndex + count;
					child.children = new ArrayList<SuffixTrieNode>();
					if (subKey.charAt(0) < subText.charAt(0)) {
						child.children.add(node);
						child.children.add(geneSubText);
					} else {
						child.children.add(geneSubText);
						child.children.add(node);
					}
				}
				flag = true;
				break;
			}
		}
		if (!flag) {
			SuffixTrieNode node = new SuffixTrieNode(key);
			node.leaf = true;
			node.firstStartIndex = startIndex;
			pointNode.children.add(node);
		}
	}
    //this method used for pattern matching
	//and the algorithm references the pseudo-code from page 24 of week10 note
	//as the lecture note write that this suffix tree and this algorithm Support arbitrary pattern matching queries 
	//in X in O(ds) time, where s is the size of the pattern, and d is the alphabet size 
	//since for this assignment we just need to process the character of 'A','C','G','T'.
	//thus the running time of this method is O(|s|).
	public int findString(String pattern){
		int patternLen = pattern.length();
		int patternCharIndex = 0;
		boolean flag = false;
		SuffixTrieNode searchNode = this.root;
		while(!flag||searchNode.leaf){
			flag = true;
			// for  each child  w of searchNode 
			for(SuffixTrieNode w:searchNode.children){
				// w.firstStartIndex is the start index of w
				int nodeStartIndex = w.firstStartIndex;
				// process child w 
				if(pattern.charAt(patternCharIndex)==w.text.charAt(0)){
					int nodeTextLen = w.text.length();
					
					 // suffix is shorter than or of the same length of the node label
					if(patternLen<=nodeTextLen){
						if(pattern.substring(patternCharIndex, patternCharIndex+patternLen).equals(w.text.substring(0, patternLen)))
							return nodeStartIndex-patternCharIndex;
						else return -1;
					}
					else{  //// suffix is longer than the node label
						if(pattern.substring(patternCharIndex, patternCharIndex+nodeTextLen).equals(w.text.substring(0, nodeTextLen))){
							patternLen = patternLen-nodeTextLen;   // update suffix length
							patternCharIndex = patternCharIndex+nodeTextLen;  // update suffix start index
							searchNode = w;
							flag = false;
							break;
						}
					}
				}
			}
		}
		return -1;
	}
	
	
	//this method used to find the LCS for f1 and f2, and the algorithm references
	//the the pseudo-code from page 56 of week10 note
	//Time Complexity:
	//in order to compute the length of LCS, 
	//the running time of initializing the array length[][] is O(m+n),, where m and n are the 
	//size of f1 and f2 respectively.
	//then using two nested loops
	//The outer one iterates n times
    //The inner one iterates m times
    //A constant amount of work is done inside each iteration of the inner loop
	//thus, this time complexity is O(m*n)
	//then using one while loop to recover the data of LCS, for this step the 
	//time complexity is O(m) or O(n).
	//conclusion: for this method the time complexity is O(m*n).
	public static String LCS(String f1, String f2) {
		int length_f1 = f1.length();
		int length_f2 = f2.length();
		
		//create array to record the length of LCS of f1[i..length_f1] and f2[j..length_f2]
		int[][] Length = new int[length_f1 + 1][length_f2 + 1];
		for(int i = 1; i <= length_f1; i++){
			Length[i][0] = 0;
		}
		for(int j = 1; j <= length_f2; j++){
			Length[0][j] = 0;
		}
		// compute length of LCS via dynamic programming
		for (int i = 1; i <= length_f1; i++) {
			for (int j = 1; j <= length_f2; j++) {
				if (f1.charAt(i - 1) == f2.charAt(j - 1))
					Length[i][j] = Length[i - 1][j - 1] + 1;
				else
					Length[i][j] = Math.max(Length[i - 1][j], Length[i][j - 1]);
			}
		}
        

		String s = "";
		int max_length = Length[length_f1][length_f2]; 
        int i =length_f1, j =length_f2;  
        
        while(max_length > 0){  
            if(Length[i][j]!=Length[i-1][j-1]){  
            	//if these two characters are same, it is the common character 
                if(Length[i-1][j]==Length[i][j-1]){  
                    s = s + f1.charAt(i - 1);  
                    max_length--;  
                    i--;
                    j--;  
                }else{  
                	//select the long sequence between these two sequences as the lcs
                    if(Length[i-1][j]>Length[i][j-1]){  
                        i--;  
                    }else{  
                        j--;  
                    }  
                }  
            }else{  
                i--;
                j--;  
            } 
            
        }  
        return s;
	}
	
	//this method used to calculate the degree of similarity for two file,
	//for the step of invoke the LCS method ,the time complexity is O(m*n),see LCS method for more detail.
	//and the time complexity of the other steps can be seem as O(1).
	//therefore, the time complexity of this method is O(m*n).
	public static double similarityAnalyser(String file2, String file3, String file4) throws IOException{
		String f1 = ReadFile(file2 + ".txt");
		String f2 = ReadFile(file3 + ".txt");
		
		//call LCS method to obtain the lcs for these two string
		//time complexity for this method is O(m*n).
		String s = LCS(f1,f2);
		File f = new File(file4 + ".txt");
		//if output file already exist
		if (f.exists()) {
			System.out.println("File4 already exists");
		}
		
		//write the data into the file 
		FileWriter output = new FileWriter(f);
		output.write(s);
		output.flush();
		output.close();
		
		//obtain the length of these three file
		double length_lcs = f.length();
		double length_S1 = f1.length();
		double length_S2 = f2.length();
		double similarity;
		//calculate the degree of similarity of two sequences  
		similarity = length_lcs/(Math.max(length_S1, length_S2));
		return similarity;

	}
	
	//read the data from file based on the relevant rules
	public static String ReadFile(String file) throws IOException{
		File f1 = new File(file);
		//if file does not exist
		if (!f1.exists()) {
			System.out.println("File does not exist");
		}
		//read data from file
		BufferedReader readTxt = new BufferedReader(new FileReader(file));
		String textLine = "";
		String S = "";

		while ((textLine = readTxt.readLine()) != null) {
			for (int i = 0; i < textLine.length(); i++) {
				// All the characters of the file are A, C, G and T
				if (textLine.charAt(i) == 'A' || textLine.charAt(i) == 'C'|| textLine.charAt(i) == 'G'|| textLine.charAt(i) == 'T'){
					S = S.concat(String.valueOf(textLine.charAt(i)));
				}
			}
		}
		readTxt.close();
		return S;
	} 
    

 
		public static void main(String[] args) throws Exception {
			
			CompressedSuffixTrie trie1 = new CompressedSuffixTrie("/Users/Jason/Desktop/file1");
	        
			System.out.println("ACTTCGTAAG is at: " + trie1.findString("ACTTCGTAAG"));

			System.out.println("AAAACAACTTCG is at: " + trie1.findString("AAAACAACTTCG"));
			        
			System.out.println("ACTTCGTAAGGTT : " + trie1.findString("ACTTCGTAAGGTT"));
			        
			System.out.println(CompressedSuffixTrie.similarityAnalyser("/Users/Jason/Desktop/file2", "/Users/Jason/Desktop/file3", "/Users/Jason/Desktop/file4"));
			
			
			
		}
	
}
