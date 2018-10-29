package sort;

import java.util.Arrays;
/**
 * 希尔排序
 * 思路：
 * 将待排序数组按照步长d进行分组，然后将每组的元素利用直接插入排序的方法进行排序；每次再将步长d折半减小，循环上述操作；
 * 当步长d=1时，利用直接插入，完成排序。
 * @author 54060
 */
public class ShellSort {

   public static void sort(int arr[]) {
      int d=arr.length/2;
      int x,j,k=1;

      while(d>=1) {
         for(int i=d;i<arr.length;i++) {
            x=arr[i];
            j=i-d;

            //直接插入排序，会向前找适合的位置
            while(j>=0 && arr[j]>x) {
                //交换位置
                arr[j+d]=arr[j];
                j=j-d;
            }
            arr[j+d]=x;
         }
         d=d/2; //步长折半
         System.out.println("第["+(k++)+"]轮，排序结果:"+Arrays.toString(arr));
      }

   }
   public static void main(String[] args) {
	      int arr[]={32,24,95,45,75,22,95,49,3,76,56,11,37,58,44,19,81};

	      System.out.println("排序前："+Arrays.toString(arr));

	      sort(arr);

	      System.out.println("排序后："+Arrays.toString(arr));

	}

}