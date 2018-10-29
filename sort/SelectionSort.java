package sort;

import java.util.Arrays;

/**
 * 选择排序
 * 1.找到数组中最小的那个元素
 * 2.将最小的这个元素和数组中第一个元素交换位置
 * 3.在剩下的元素中找到最小的的元素，与数组第二个元素交换位置
 * 重复以上步骤，即可以得到有序数组。
 * @author 54060
 *
 */
public class SelectionSort {
	//{ 5,3,6,2,10 }->{2,3,6,5,10}->{2,3,6,5,10}->{2,3,5,6,10}->{2,3,5,6,10}
    public static void selectionSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n; i++) {
            int k = i;
            // 找出最小值的索引
            for (int j = i + 1; j < n; j++) {
                if (arr[j] < arr[k]) {
                    k = j;
                }
            }
            // 将最小值放到排序序列开头（调换位置）
            if (k > i) {//如果第i个就是最小的，则不用交换了
                int tmp = arr[i];
                arr[i] = arr[k];
                arr[k] = tmp;
            }
        }
    }
 
    public static void main(String[] args) {
        int[] arr = {5,3,6,2,10};
        System.out.println("排序前："+Arrays.toString(arr));
        selectionSort(arr);
        System.out.println("排序后："+Arrays.toString(arr));
    }
}
