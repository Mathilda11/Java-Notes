package sort;

/**
 * 冒泡排序
 * 思路：
 * 依次比较相邻的两个数，将小数放在前面，大数放在后面。即在第一趟：首先比较第1个和第2个数，将小数放前，大数放后。然后比较第2个数和第3个数，将小数放前，大数放后，如此继续，直至比较最后两个数，将小数放前，大数放后。重复第一趟步骤，直至全部排序完成。
 * 第一趟比较完成后，最后一个数一定是数组中最大的一个数，所以第二趟比较的时候最后一个数不参与比较；
 * 第二趟比较完成后，倒数第二个数也一定是数组中第二大的数，所以第三趟比较的时候最后两个数不参与比较；
 * 依次类推，每一趟比较次数-1；
 * ……
 */
import java.util.Arrays;

public class BubbleSort {
	public static void bubbleSort(int[] arr){
		int temp=0;
		int flag=0;
		for(int i=0;i<arr.length-1;i++) {
			flag=0;
			for(int j=0;j<arr.length-1-i;j++) {
				if(arr[j]>arr[j+1]) {
					temp=arr[j+1];
					arr[j+1]=arr[j];
					arr[j]=temp;
					flag=1;
				}
			}
			if(flag==0) { //如果未进行交换，说明已排好序，退出继续排序。
		         break;
		    }
			System.out.println("第["+(i+1)+"]轮，排序结果:"+Arrays.toString(arr));
		}
	}
	
	public static void main(String[] args) {

	      //int arr[]= {65,58,95,10,57,62,13,106,78,23,85};
		  //int arr[]= {11,22,33,44,55,66};
		  int arr[] = {33,22,11,44,55,66};
	      System.out.println("排序前："+Arrays.toString(arr));
	      System.out.println("------------------------------");
	      bubbleSort(arr);
	      System.out.println("------------------------------");
	      System.out.println("排序后："+Arrays.toString(arr));

	}
}
