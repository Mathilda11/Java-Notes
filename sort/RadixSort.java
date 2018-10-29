package sort;

import java.util.Arrays;

/**
 * 基数排序
 * LSD从最右侧低位开始进行排序。先从kd开始排序，再对kd-1进行排序，依次重复，直到对k1排序后便得到一个有序序列。
 * LSD方式适用于位数少的序列。
 * 
 * 具体算法描述如下：
 * 取得数组中的最大数，并取得位数；
 * arr为原始数组，从最低位开始取每个位组成radix数组；
 * 对radix进行计数排序（利用计数排序适用于小范围数的特点）。
 * 
 * https://www.cnblogs.com/morethink/p/8419151.html#%E5%B8%8C%E5%B0%94%E6%8E%92%E5%BA%8F
 * @author 54060
 *
 */
public class RadixSort {
	public static void radixSort(int[] arr) {
	    if (arr.length <= 1) return;

	    //取得数组中的最大数，并取得位数
	    int max = 0;
	    for (int i = 0; i < arr.length; i++) {
	        if (max < arr[i]) {
	            max = arr[i];
	        }
	    }
	    int maxDigit = 1;
	    while (max / 10 > 0) {
	        maxDigit++;
	        max = max / 10;
	    }
	    //申请一个桶空间
	    int[][] buckets = new int[10][arr.length];
	    int base = 10;

	    //从低位到高位，对每一位遍历，将所有元素分配到桶中
	    for (int i = 0; i < maxDigit; i++) {
	        int[] bktLen = new int[10];        //存储各个桶中存储元素的数量

	        //分配：将所有元素分配到桶中
	        for (int j = 0; j < arr.length; j++) {
	            int whichBucket = (arr[j] % base) / (base / 10);
	            buckets[whichBucket][bktLen[whichBucket]] = arr[j];
	            bktLen[whichBucket]++;
	        }

	        //收集：将不同桶里数据挨个捞出来,为下一轮高位排序做准备,由于靠近桶底的元素排名靠前,因此从桶底先捞
	        int k = 0;
	        for (int b = 0; b < buckets.length; b++) {
	            for (int p = 0; p < bktLen[b]; p++) {
	                arr[k++] = buckets[b][p];
	            }
	        }
	        base *= 10;
	    }
	}
	public static void main(String[] args) {
		int[] arr = {3,44,38,5,47,15,36,26,27,2,46,4,19,50};
		System.out.println("排序前: " + Arrays.toString(arr));
		radixSort(arr);
        System.out.println("排序后: " + Arrays.toString(arr));
	}
}
