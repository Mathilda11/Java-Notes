package sort;

/**
 * 归并排序
 * 归并排序（MERGE-SORT）是利用归并的思想实现的排序方法，该算法采用经典的分治（divide-and-conquer）策略（分治法将问题分(divide)成一些小的问题然后递归求解，而治(conquer)的阶段则将分的阶段得到的各答案"修补"在一起，即分而治之)。
 * 思路：递归法（假设序列共有n个元素）：
 * 1.将序列每相邻两个数字进行归并操作，形成 floor(n/2)个序列，排序后每个序列包含两个元素；
 * 2.将上述序列再次归并，形成 floor(n/4)个序列，每个序列包含四个元素；
 * 3.重复步骤2，直到所有元素排序完毕。
 * 
 * 归并排序的最好，最坏，平均时间复杂度均为O(nlogn)。
 * @author 54060
 *
 */
import java.util.Arrays;

public class MergeSort {

    public static void sort(int []arr){
        int[] temp = new int[arr.length];//在排序前，先建好一个长度等于原数组长度的临时数组，避免递归中频繁开辟空间
        sort(arr,0,arr.length-1,temp);
    }
    private static void sort(int[] arr,int left,int right,int []temp){
        if(left<right){
            int mid = left + (right-left)/2;
            sort(arr,left,mid,temp);//左边归并排序，使得左子序列有序
            sort(arr,mid+1,right,temp);//右边归并排序，使得右子序列有序
            merge(arr,left,mid,right,temp);//将两个有序子数组合并操作
        }
    }
    //4 8
    //5 7
    //4 5 7 8
    //1 3
    //2 6
    //1 2 3 6
    //1 2 3 4 5 6 7 8
    private static void merge(int[] arr,int left,int mid,int right,int[] temp){
        int i = left;//左序列指针
        int j = mid+1;//右序列指针
        int t = 0;//临时数组指针
        while (i<=mid && j<=right){
            if(arr[i]<=arr[j]){
                temp[t++] = arr[i++];
            }else {
                temp[t++] = arr[j++];
            }
        }
        while(i<=mid){//将左边剩余元素填充进temp中
            temp[t++] = arr[i++];
        }
        while(j<=right){//将右序列剩余元素填充进temp中
            temp[t++] = arr[j++];
        }
        t = 0;
        //将temp中的元素全部拷贝到原数组中
        while(left <= right){
            arr[left++] = temp[t++];
        }
        System.out.println("归并结果:"+Arrays.toString(arr));
    }
    
    public static void main(String []args){
        int []arr = {8,4,5,7,1,3,6,2};
	    System.out.println("排序前："+Arrays.toString(arr));
        sort(arr);
        System.out.println("排序后："+Arrays.toString(arr));
    }
}