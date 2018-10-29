package sort;

import java.util.Arrays;

/**
 * 堆排序
 * @author 54060
 *
 */
public class HeapSort {
 
	/*
	 * 创建小顶堆：双亲节点小于子节点的值。从叶子节点开始，直到根节点。这样建立的堆定位最小值
	 */
	private void createLittleHeap(int[] data, int last) {
		 for (int i = (last- 1) / 2; i >= 0; i--) {  //找到最后一个叶子节点的双亲节点
	            // 保存当前正在判断的节点  
	            int parent = i;  
	            // 若当前节点的左子节点存在，即子节点存在
	            while (2 * parent + 1 <= last) {  
	                // biggerIndex总是记录较小节点的值,先赋值为当前判断节点的左子节点  
	                int little = 2 * parent + 1;//bigger指向左子节点  
	                if (little < last) { //说明存在右子节点
	                  
	                    if (data[little] > data[little+ 1]) { //右子节点>左子节点时
	                     
	                    	little=little+1;  //little取左子节点和右子节点最小的那个。因为父节点要小于这两个节点的值
	                    }  
	                } 
	                //下-->上-->下
	                if (data[parent] > data[little]) {  //若双亲节点值大于子节点中最小的
	                    // 若当前节点值比子节点最小值大，则交换2者的值，交换后将littleIndex值赋值给k  
	                    swap(data, parent, little);  
	                    parent = little;  
	                } else {  
	                    break;  
	                }  
	            }  
	        }  
	}
	/*
	 * 创建大顶堆：双亲节点大于子节点的值。从叶子节点开始，直到根节点。这样建立的堆定位最大值
	 */
	private void createBigHeap(int[] data, int last) {
		 for (int i = (last- 1) / 2; i >= 0; i--) {  //找到最后一个叶子节点的双亲节点
	            // 保存当前正在判断的节点  
	            int parent = i;  
	            // 若当前节点的左子节点存在，即子节点存在
	            while (2 * parent + 1 <= last) {  
	                // biggerIndex总是记录较大节点的值,先赋值为当前判断节点的左子节点  
	                int bigger = 2 * parent + 1;//bigger指向左子节点  
	                if (bigger < last) { //说明存在右子节点
	                  
	                    if (data[bigger] < data[bigger+ 1]) { //右子节点>左子节点时
	                     
	                        bigger=bigger+1;  //bigger取左子节点和右子节点最小的那个。因为父节点要小于这两个节点的值
	                    }  
	                } 
	                //下-->上-->下
	                if (data[parent] < data[bigger]) {  //若双亲节点值大于子节点中最小的
	                    // 若当前节点值比子节点最小值大，则交换2者的值，交换后将biggerIndex值赋值给k  
	                    swap(data, parent, bigger);  
	                    parent = bigger;  
	                } else {  
	                    break;  
	                }  
	            }  
	        }  
	}

	 public  void swap(int[] data, int i, int j) {  
	        if (i == j) {  
	            return;  
	        } 
	        //只用两个数完成交换
	        data[i] = data[i] + data[j];  
	        data[j] = data[i] - data[j];  
	        data[i] = data[i] - data[j];  
	    }  
		public static void main(String[] args) {
			int arr[] = {3,1,5,7,2,4,9,6,10,8};  
			System.out.println("排序前："+Arrays.toString(arr));
			System.out.println("-------------小顶堆排序---------------");
			HeapSort heapSort = new HeapSort();
			for(int i=0;i<arr.length;i++){
				heapSort.createLittleHeap(arr,arr.length-1-i);//创建堆,创建的是小顶堆。每次循环完，二叉树的根节点都是最小值，所以与此时的未排好部分最后一个值交换位置
				heapSort.swap(arr, 0, arr.length - 1 - i);//与最后一个值交换位置，最小值找到了位置
				System.out.println("第["+(i+1)+"]轮，排序结果:"+Arrays.toString(arr));
			}
			System.out.println("排序后："+Arrays.toString(arr));
			System.out.println("-------------大顶堆排序---------------");
			for(int i=0;i<arr.length;i++){
				heapSort.createBigHeap(arr,arr.length-1-i);//创建堆,创建的是小顶堆。每次循环完，二叉树的根节点都是最小值，所以与此时的未排好部分最后一个值交换位置
				heapSort.swap(arr, 0, arr.length - 1 - i);//与最后一个值交换位置，最小值找到了位置
				System.out.println("第["+(i+1)+"]轮，排序结果:"+Arrays.toString(arr));
			}
			System.out.println("排序后："+Arrays.toString(arr));
		}
}