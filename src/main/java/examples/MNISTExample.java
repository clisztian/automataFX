package examples;

import java.io.IOException;

import tsetlin.ConvolutionEncoder;
import tsetlin.MultivariateConvolutionalAutomatonMachine;

public class MNISTExample {

	public static void main(String[] args) throws IOException {
		
		
		int num_samples = 59000;
		
		int dimX = 28;
		int dimY = 28;
		int patchX = 10; 
		int patchY = 10;
		
		ConvolutionEncoder myEncoder = new ConvolutionEncoder(dimX, dimY, 1, patchX, patchY);
		int[][] X_encoder = myEncoder.bit_encoder(num_samples, "/home/lisztian/fast-tsetlin-machine-with-mnist-demo/MNISTTraining.txt", ' ');
		int[] label = myEncoder.getLabels();
		
		System.out.println("Num samples: " + label.length);
		int threshold = 5000;
		int nClauses = 500;
		float S = 10f;
		float max_specificity = 10f;
		int nClasses = 10;

		MultivariateConvolutionalAutomatonMachine conv = new MultivariateConvolutionalAutomatonMachine(myEncoder, threshold, nClasses, nClauses, max_specificity, true); 
		
		long start = System.currentTimeMillis();
		for(int i = 0; i < 1000; i++) {		
			int pred = conv.update(X_encoder[i], label[i]);	
			System.out.println(i + " " + pred + " " + label[i]);
		}
		long end = System.currentTimeMillis();
		
		System.out.println((float)(end - start)/1000f);
		

		for(int i = 0; i < 100; i++) {
			int[] local_pred = conv.predict_interpret(X_encoder[1000+i]);
			System.out.println("Class predict: " + local_pred[local_pred.length - 1] + " " + label[1000+i]);
		}
		
		
		
		
	}
	
	
}
