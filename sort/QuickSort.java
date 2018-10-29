package sort;

import java.util.Arrays;

/**
 * 快速排序
 * 思路：
 * 选择一个关键值作为基准值。比基准值小的都在左边序列（一般是无序的），比基准值大的都在右边（一般是无序的）。一般选择序列的第一个元素。
 * 一次循环：从后往前比较，用基准值和最后一个值比较，如果比基准值小的交换位置，如果没有继续比较下一个，直到找到第一个比基准值小的值才交换。
 * 找到这个值之后，又从前往后开始比较，如果有比基准值大的，交换位置，如果没有继续比较下一个，直到找到第一个比基准值大的值才交换。
 * 直到从前往后的比较索引>从后往前比较的索引，结束第一次循环，此时，对于基准值来说，左右两边就是有序的了。
 * 接着分别比较左右两边的序列，重复上述的循环。
 * 
 * http://blog.51cto.com/13733462/2113397
 * @author 54060
 *
 */
public class QuickSort {

	   public static void quickSort(int [] arr,int left,int right) {

	      int pivot=0;

	      if(left<right) {

	         pivot=partition(arr,left,right);

	         quickSort(arr,left,pivot-1); //左子数组排序

	         quickSort(arr,pivot+1,right); //右子数组排序

	      }

	   }

	 

	   private static int partition(int[] arr,int left,int right) {

	      int key=arr[left];

	      while(left<right) {
	    	  
	    	 //如果arr[right]>key则我们只需要将right--，right--之后，再拿arr[right]与key进行比较，
	    	 //直到arr[right]<key交换元素为止。
	         while(left<right && arr[right]>=key) {

	            right--;

	         }

	         arr[left]=arr[right];  //如果arr[right]<key，则arr[left]=arr[right]将这个比key小的数放到左边去.
	         
	         //如果右边存在arr[right]<key的情况，将arr[left]=arr[right]。
	         //接下来，将转向left端，拿arr[left ]与key进行比较，
	         //如果arr[left]>key,则将arr[right]=arr[left]，如果arr[left]<key，则只需要将left++,
	         //然后再进行arr[left]与key的比较。
	         while(left<right && arr[left]<=key) {

	            left++;

	         }

	         arr[right]=arr[left];

	      }

	      arr[left]=key;

	      return left;

	   }

	  

	   public static void main(String[] args) {

	      int arr[]= {65,58,95,10,57,62,13,106,78,23,85};

	      System.out.println("排序前："+Arrays.toString(arr));

	      quickSort(arr,0,arr.length-1);

	      System.out.println("排序后："+Arrays.toString(arr));

	   }

	}
