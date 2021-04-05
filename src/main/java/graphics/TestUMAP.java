package graphics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import tagbio.umap.Umap;

public class TestUMAP {

	
	public static void main(String[] args) throws IOException {
  
		
		float[][] myd = getDigitData("/home/lisztian/AutomataFX/workspace/automataFX/src/main/resources/data/digits.tsv");
		
		final Umap umap = new Umap();
		umap.setNumberComponents(3);         // number of dimensions in result
		umap.setNumberNearestNeighbours(15);
		umap.setThreads(20);
		
		final float[][] result = umap.fitTransform(myd);
		
		for(int i = 0; i < result.length; i++) {
			
			for(int j = 0; j < result[i].length; j++) {
				System.out.print(result[i][j] + " ");
			}
			System.out.println();
			
		}
		
	}
	
	
	public static float[][] getDigitData(String fileName) throws IOException {
		
		ArrayList<float[]> digits = new ArrayList<float[]>();	
		List<String> allLines = Files.readAllLines(Paths.get(fileName));
		float[][] my_data = new float[allLines.size() - 1][];
		int[] labels = new int[allLines.size() - 1];
		for(int i = 1; i < allLines.size(); i++) {
			
			String line = allLines.get(i);
			String[] obs = line.split("\t", -1);

			my_data[i-1] = new float[obs.length-1];
			
			labels[i-1] = Integer.parseInt(obs[0].split("[:]+")[0]);
			for(int k = 1; k < obs.length; k++) {
				my_data[i-1][k-1] = Float.parseFloat(obs[k]);
			}
			
		}
		
		return my_data;
	}
	
	
}
