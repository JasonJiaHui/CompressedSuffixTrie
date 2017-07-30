package archive;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

public class CompressedSuffixTrie {

	String text = "";
    Suffixtrie SuffixTrie;
    int childidx = -1;

    // construct a CompressedSuffixTrie from file
    public CompressedSuffixTrie(String f) 
    {
        int chrArr[] = {'A', 'C', 'G', 'T'};
        int readcounter;
        StringBuilder builder = new StringBuilder();
        File file = new File(f);     
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((readcounter = input.read()) != -1) {
                for (int i = 0; i < chrArr.length; i++) {
                    if (readcounter == chrArr[i]) {
                        char chr = (char) readcounter;
                        builder.append(chr);
                        break;
                    }
                }
            }
            input.close();
            text = builder.toString();
            //Initial suffix tree... each index refers a suffix string
            SuffixTrie = new Suffixtrie(-1, 0, "", text);
            for (int index = 0; index < text.length(); index++) {
                String str = text.substring(index);
                SuffixTrie.insert(index, str, 0, text);
            }
   
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Method for finding the first occurrence of a pattern s in the DNA sequence */ 
    public int findString(String s) {
        childidx = -1;
        int p = s.length();
        int j = 0;
        Suffixtrie v = SuffixTrie;
        boolean f = true;
        int gap = 0;
        while (f) {
            f = true;
            Suffixtrie childofv = v.child;
            while (childofv != null) {
                int i = childofv.starti;
                if (s.charAt(j) == text.charAt(i)) {
                    int x = childofv.endi - i + 1;
                    if (p <= x) {
                        if (s.substring(j, j + p).equals(text.substring(i, i + p))) {
                            if (childofv.index == -1) {
                                childlongests(childofv.child);
                                return childidx;
                            } else {
                                return childofv.index + gap - j;
                            }
                        } else {
                            return -1;
                        }
                    } else {
                        if (s.substring(j, j + x).equals(text.substring(i, i + x))) {
                            p = p - x;
                            j = j + x;
                            gap = gap + x;
                            v = childofv;
                            // f=false;
                            if (childofv.isLeaf() == true) {
                                f = false;
                            }
                            break;
                        } else {
                            return -1;
                        }
                    }
                } else {
                    childofv = childofv.silbing;
                }
            }
            if (childofv == null) {
                f = false;
            }
        }
        return -1;
    }

    public void childlongests(Suffixtrie v) {
        if ((childidx == -1 || childidx > v.index) && v.index != -1) {
            childidx = v.index;
        }
        if (v.child != null) {
            childlongests(v.child);
        }
        if (v.silbing != null) {
            childlongests(v.silbing);
        }
    }
    /** Method for computing the degree of similarity of two DNA sequences stored
    in the text files f1 and f2 */ 
    public static float similarityAnalyser(String f1, String f2, String f3) {
        int chrArr[] = {'A', 'C', 'G', 'T'};
        int readcounter;
        float similarity = 0;
        StringBuilder builder1 = new StringBuilder();
        StringBuilder builder2 = new StringBuilder();
        File file1 = new File(f1);
        File file2 = new File(f2);
        String lcs = "";
        try {
            BufferedReader input1 = new BufferedReader(new InputStreamReader(new FileInputStream(file1)));

            while ((readcounter = input1.read()) != -1) {
                for (int i = 0; i < 4; i++) {
                    if (readcounter == chrArr[i]) {
                        char chr1 = (char) readcounter;
                        builder1.append(chr1);
                        break;
                    }
                }
            }
            String text1 = builder1.toString();
            input1.close();
            readcounter = 0;
      
            BufferedReader input2 = new BufferedReader(new InputStreamReader(new FileInputStream(file2)));

            while ((readcounter = input2.read()) != -1) {
                for (int i = 0; i < 4; i++) {
                    if (readcounter == chrArr[i]) {
                        char chr2 = (char) readcounter;
                        builder2.append(chr2);
                        break;
                    }
                }
            }
            String text2 = builder2.toString();
            input2.close();
            int n = text1.length();
            int m = text2.length();


            int[][] lcs_length = new int[n + 1][m+ 1];
            for (int i= 0; i < n ; i++) {
                for (int j = 0; j < m; j++) {
                    if (text1.charAt(i) == text2.charAt(j)) {
                        lcs_length[i + 1][j + 1] = lcs_length[i][j] + 1;
                    } else {
                        lcs_length[i + 1][j + 1] = Math.max(lcs_length[i][j + 1], lcs_length[i + 1][j]);
                    }
                }
            }
         
            StringBuilder lcsbuilder = new StringBuilder();
            while (n != 0 && m != 0) {
                if (lcs_length[n][m] == lcs_length[n - 1][m]) 
                    n--;
                else if (lcs_length[n][m] == lcs_length[n][m - 1])
                    m--;
                else {
                    assert text1.charAt(n - 1) == text2.charAt(m - 1);
                    lcsbuilder.append(text1.charAt(n - 1));
                    n--;
                    m--;
                }
            }
            lcs = lcsbuilder.reverse().toString();
            
            if (text1.length() > text2.length()){
            	similarity =  (float)lcs.length() / (float)text1.length();
            }else{
            	similarity = (float) lcs.length() / (float)text2.length() ;
            }

            File file3 = new File(f3);
            FileOutputStream outputFile = new FileOutputStream(file3);
            outputFile.write(lcs.getBytes());
            outputFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
            return similarity;
        }
    }

// suffixtrie class
class Suffixtrie {
    String entry; 
    Suffixtrie child; 
    Suffixtrie silbing;
    int index; 
    int depth; 
    int starti; 
    int endi; 


    Suffixtrie(int index, int depth, String entry,  String text) {
        this.index = index;
        this.entry = entry;
        this.starti = text.indexOf(entry);
        this.endi = this.starti + entry.length() - 1;
        this.depth = depth;     
    }

    // renew the level
    void reLevel() {
        Suffixtrie v;
        this.depth++;
        if (this.isLeaf()) {
            return;
        }
        v = this.child;
        while (v != null) {
            v.reLevel();
            v = v.silbing;
        }
    }
    
    
// check the node whether is a leaf or not.
    public boolean isLeaf() {
        if (this.child == null) {
            return true;
        } else {
            return false;
        }
    }
 // insert a entry into Suffixtrie
    void insert(int index, String entry, int depth, String text) {
        Suffixtrie nNode, vNode, pNode;
        String strtemp, prefix;
        int idx;
        if (entry.length() < 1) {
            return;
        }
        if (this.isLeaf()) {
            nNode = new Suffixtrie(index, depth + 1, entry,  text);
            this.child = nNode;
            return;
        }
        vNode = this.child;
        if (vNode.entry.charAt(0) > entry.charAt(0)) {
            nNode = new Suffixtrie(index, depth + 1, entry,  text);
            this.child = nNode;
            nNode.silbing = vNode;
            return;
        }
        pNode = vNode;
        while ((vNode != null) && (vNode.entry.charAt(0) < entry.charAt(0))) {
            pNode = vNode;
            vNode = vNode.silbing;
        }
        if (vNode == null) {
            nNode = new Suffixtrie(index,depth + 1, entry,  text);
            pNode.silbing = nNode;
            return;
        }
        if (vNode.entry.charAt(0) > entry.charAt(0)) {
            nNode = new Suffixtrie(index, depth + 1, entry, text);
            pNode.silbing = nNode;
            nNode.silbing = vNode;
            return;
        }
        int size = 0;
        if (vNode.entry.length() >= entry.length()) {
            size = entry.length();
        } else {
            size = vNode.entry.length();
        }
        for (idx = 1; idx < size; idx++) {
            if (entry.length() <= 1) {
                break;
            }
            if (vNode.entry.charAt(idx) != entry.charAt(idx)) {
                break;
            }
        }
        if (idx == vNode.entry.length()) {
            strtemp = entry.substring(idx);
            if (entry.equals(vNode.entry)) {
                return;
            }
            if (strtemp.length() > 0) {
                vNode.insert(index, strtemp, depth + 1, text);
            }
            return;
        }
        prefix = vNode.entry.substring(0, idx);
        strtemp = vNode.entry.substring(idx);
        pNode = new Suffixtrie(vNode.index, depth + 1, strtemp, text);
        pNode.child = vNode.child;
        vNode.index = -1;
        vNode.child = pNode;
        vNode.entry = prefix;
        vNode.starti = text.indexOf(prefix);
        vNode.endi = vNode.starti + prefix.length() - 1;
        pNode.reLevel();
        strtemp = entry.substring(idx);
        vNode.insert(index, strtemp, depth + 1, text);
        return;
    }
    
    public static void main(String[] args) {
		
    	CompressedSuffixTrie trie1 = new CompressedSuffixTrie("file1.txt");
        
    	System.out.println("ACTTCGTAAG is at: " + trie1.findString("ACTTCGTAAG"));

    	System.out.println("AAAACAACTTCG is at: " + trie1.findString("AAAACAACTTCG"));
    	        
    	System.out.println("ACTTCGTAAGGTT : " + trie1.findString("ACTTCGTAAGGTT"));
    	        
//    	System.out.println(CompressedSuffixTrie.similarityAnalyser("file2.txt", "file3.txt", "file4.txt"));
    	
	}
	
}
