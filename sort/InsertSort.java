package sort;

import java.util.Arrays;

/**
 * 插入排序
 * 插入排序类似整理扑克牌，将每一张牌插到其他已经有序的牌中适当的位置。
 * 插入排序由N-1趟排序组成，对于P=1到N-1趟，插入排序保证从位置0到位置P上的元素为已排序状态。
 * 简单的说，就是插入排序总共需要排序N-1趟，从index为1开始，讲该位置上的元素与之前的元素比较，放入合适的位置，这样循环下来之后，即为有序数组。
 * @author 54060
 *
 */
public class InsertSort { 
	  public static void InsertSort(int[] arr) { 
	    int i, j; 
	    int insertNode;// 要插入的数据 
	    // 从数组的第二个元素开始循环将数组中的元素插入 
	    for (i = 1; i < arr.length; i++) { 
	      // 设置数组中的第2个元素为第一次循环要插入的数据 
	      insertNode = arr[i]; 
	      j = i - 1; 
	      // 如果要插入的元素小于第j个元素，就将第j个元素向后移 
	      while ((j >= 0) && insertNode < arr[j]) { 
	        arr[j + 1] = arr[j]; 
	        j--;  
	      } 
	      // 直到要插入的元素不小于第j个元素,将insertNote插入到数组中 
	      arr[j + 1] = insertNode; 
	      System.out.println("第["+i+"]轮，排序结果:"+Arrays.toString(arr));
	    } 
	  } 
	  
	  public static void main(String[] args) { 
	    int arr[] = { 53, 27, 36, 15, 69, 42 }; 
		System.out.println("排序前："+Arrays.toString(arr));
		
	    InsertSort(arr); 
	  
		System.out.println("排序后："+Arrays.toString(arr));
	  } 
	  
	} 