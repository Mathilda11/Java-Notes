@[toc]
## 简介
ArrayList的底层是数组队列，相当于动态数组。与Java中的数组相比，它的容量能动态增长。在添加大量元素前，应用程序可以使用ensureCapacity操作来增加ArrayList实例的容量。这可以减少递增式再分配的数量。
```javascript
public class ArrayList<E> extends AbstractList<E>
        implements List<E>, RandomAccess, Cloneable, java.io.Serializable 
```
ArrayList继承了AbstractList，实现了List。它是一个数组队列，提供了相关的添加、删除、修改、遍历等功能。
ArrayList实现了RandomAccess接口，提供了随机访问功能。RandomAccess只是标识该接口的实现类支持随机访问，并没有实际的方法。在ArrayList中，我们可以通过元素下标快速获取元素对象，这就是快速随机访问。
ArrayList实现了Cloneable接口，即覆盖了函数clone()，能被克隆。
ArrayList实现java.io.Serializable接口，这意味着ArrayList支持序列化，能通过序列化去传输。

和Vector不同，ArrayList中的操作不是线程安全的！所以，建议在单线程中才使用ArrayList，而在多线程中可以选择Vector或者 CopyOnWriteArrayList。

## ArrayList的源码分析

### 初始化
1. 空参构造方法
```javascript
    /**
     * Constructs an empty list with an initial capacity of ten.
     */
    public ArrayList() {
        this.elementData = DEFAULTCAPACITY_EMPTY_ELEMENTDATA;
    }
```

elementData是存储ArrayList元素的数组缓冲区。
```javascript
/**
* The array buffer into which the elements of the ArrayList are stored.
* The capacity of the ArrayList is the length of this array buffer. Any
* empty ArrayList with elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA
* will be expanded to DEFAULT_CAPACITY when the first element is added.
*/
transient Object[] elementData;
```
DEFAULTCAPACITY_EMPTY_ELEMENTDATA是用于默认大小空实例的共享空数组实例。
```javascript
/**
 * Shared empty array instance used for default sized empty instances. We
 * distinguish this from EMPTY_ELEMENTDATA to know how much to inflate when
 * first element is added.
 */
 private static final Object[] DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {};
```
以上实现了将elementData指向一个空数组。

2. 带参构造函数
```javascript
    /**
     * Constructs an empty list with the specified initial capacity.
     *
     * @param  initialCapacity  the initial capacity of the list
     * @throws IllegalArgumentException if the specified initial capacity
     *         is negative
     */
    public ArrayList(int initialCapacity) {
        if (initialCapacity > 0) {
            this.elementData = new Object[initialCapacity];
        } else if (initialCapacity == 0) {
            this.elementData = EMPTY_ELEMENTDATA;
        } else {
            throw new IllegalArgumentException("Illegal Capacity: "+
                                               initialCapacity);
        }
    }
```
这是给定初始化容量的构造方法。首先对传入的初始化容量initialCapacity判断，如果该容量大于0，创建一个容量为initialCapacity的数组，并将elementData指向该数组；如果该容量等于0，则将elementData指向EMPTY_ELEMENTDATA，一个空数组。
```javascript
    /**
     * Shared empty array instance used for empty instances.
     */
    private static final Object[] EMPTY_ELEMENTDATA = {};
```
还有一种初始化方法是给定一个集合。此时elementData即为将集合c转成数组，size即为elementData的长度。如果size不等于0且elementData不是Object型数组，则利用Arrays.copyOf方法，将elementData指向一个新的Object型数组；如果size等于0，则将elementData指向空数组。
```javascript
    /**
     * Constructs a list containing the elements of the specified
     * collection, in the order they are returned by the collection's
     * iterator.
     *
     * @param c the collection whose elements are to be placed into this list
     * @throws NullPointerException if the specified collection is null
     */
    public ArrayList(Collection<? extends E> c) {
        elementData = c.toArray();
        if ((size = elementData.length) != 0) {
            // c.toArray might (incorrectly) not return Object[] (see 6260652)
            if (elementData.getClass() != Object[].class)
                elementData = Arrays.copyOf(elementData, size, Object[].class);
        } else {
            // replace with empty array.
            this.elementData = EMPTY_ELEMENTDATA;
        }
    }
```
### add
添加元素时使用 ensureCapacityInternal() 方法来保证容量足够。
```javascript
    /**
     * Appends the specified element to the end of this list.
     *
     * @param e element to be appended to this list
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        ensureCapacityInternal(size + 1);  // Increments modCount!!
        elementData[size++] = e;
        return true;
    }
```
判断elementData是否通过无参构造方法初始化的，如果是，则在DEFAULT_CAPACITY和minCapacity(size+1)中进行选择一个较大的。
```javascript
    private void ensureCapacityInternal(int minCapacity) {
        if (elementData == DEFAULTCAPACITY_EMPTY_ELEMENTDATA) {
            minCapacity = Math.max(DEFAULT_CAPACITY, minCapacity);
        }

        ensureExplicitCapacity(minCapacity);
    }
```
其中DEFAULT_CAPACITY是数组的默认大小：10。
```javascript
private static final int DEFAULT_CAPACITY = 10;
```
这里如果minCapacity小于10，则设为10，所以如果没有指定大小的话，默认是初始化一个容量为10的数组，通过源码我们发现是在执行add操作时才进行默认初始化容量的设定。

然后调用ensureExplicitCapacity方法。
```javascript
    private void ensureExplicitCapacity(int minCapacity) {
        modCount++;

        // overflow-conscious code
        if (minCapacity - elementData.length > 0)
            grow(minCapacity);
    } 
```
modCount涉及到Fail-Fast机制。
Fail-Fast机制是Java集合(Collection)中的一种错误机制，当多个线程对同一个集合进行操作的时候，某线程访问集合的过程中，该集合的内容被其他线程所改变(即其它线程通过add、remove、clear等方法，改变了modCount的值)；这时，就会抛出ConcurrentModificationException异常，产生Fail-Fast事件。

modCount用来记录ArrayList结构发生变化的次数。结构发生变化是指添加或者删除至少一个元素的所有操作，或者是调整内部数组的大小，仅仅只是设置元素的值不算结构发生变化。

在进行序列化或者迭代等操作时，需要比较操作前后 modCount 是否改变，如果改变了需要抛出 ConcurrentModificationException。


回到grow方法，判断新增元素后的大小minCapacity是否超过当前集合的容量elementData.length，如果超过，则调用grow方法进行扩容。
```javascript
    /**
     * Increases the capacity to ensure that it can hold at least the
     * number of elements specified by the minimum capacity argument.
     *
     * @param minCapacity the desired minimum capacity
     */
    private void grow(int minCapacity) {
        // overflow-conscious code
        int oldCapacity = elementData.length;
        int newCapacity = oldCapacity + (oldCapacity >> 1);
        if (newCapacity - minCapacity < 0)
            newCapacity = minCapacity;
        if (newCapacity - MAX_ARRAY_SIZE > 0)
            newCapacity = hugeCapacity(minCapacity);
        // minCapacity is usually close to size, so this is a win:
        elementData = Arrays.copyOf(elementData, newCapacity);
    }
```
首先新容量newCapacity设为 oldCapacity + (oldCapacity >> 1)，也就是旧容量的 1.5 倍。对newCapacity的大小进行判断，如果仍然小于minCapacity，则使newCapacity等于minCapacity，再判断newCapacity是否超过最大的容量，如果超过，则调用hugeCapacity方法，传入参数是minCapacity，即新增元素后需要的最小容量。
```javascript
    private static int hugeCapacity(int minCapacity) {
        if (minCapacity < 0) // overflow
            throw new OutOfMemoryError();
        return (minCapacity > MAX_ARRAY_SIZE) ?
            Integer.MAX_VALUE :
            MAX_ARRAY_SIZE;
    }
```
如果minCapacity大于MAX_ARRAY_SIZE，则返回Integer的最大值。否则返回MAX_ARRAY_SIZE。
```javascript
    /**
     * The maximum size of array to allocate.
     * Some VMs reserve some header words in an array.
     * Attempts to allocate larger arrays may result in
     * OutOfMemoryError: Requested array size exceeds VM limit
     */
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
```
然后调用Arrays.copyOf()把原数组整个复制到新容量的数组中，这个操作代价很高，因此最好在创建ArrayList对象时就指定大概的容量大小，减少扩容操作的次数。

### remove

删除操作需要调用System.arraycopy()将index+1后面的元素都复制到index位置上，该操作的时间复杂度为O(N)，可以看出ArrayList 删除元素的代价是非常高的。
```javascript
/**
* Removes the element at the specified position in this list.
* Shifts any subsequent elements to the left (subtracts one from their
* indices).
*
* @param index the index of the element to be removed
* @return the element that was removed from the list
* @throws IndexOutOfBoundsException {@inheritDoc}
*/
public E remove(int index) {
    rangeCheck(index);
    modCount++;
    E oldValue = elementData(index);
    int numMoved = size - index - 1;
    if (numMoved > 0)
        System.arraycopy(elementData, index+1, elementData, index, numMoved);
    elementData[--size] = null; // clear to let GC do its work
    return oldValue;
}
```
### System.arraycopy()和Arrays.copyOf()方法

通过以上源码，我们发现在实现数组复制时使用了不同的方法。比如，在扩容grow方法中，使用了Arrays.copyOf方法，而在remove(int index)中使用了System.arraycopy方法。

联系： 
看两者源代码可以发现copyOf()内部调用了System.arraycopy()方法。
```javascript
    public static <T,U> T[] copyOf(U[] original, int newLength, Class<? extends T[]> newType) {
        @SuppressWarnings("unchecked")
        T[] copy = ((Object)newType == (Object)Object[].class)
            ? (T[]) new Object[newLength]
            : (T[]) Array.newInstance(newType.getComponentType(), newLength);
        System.arraycopy(original, 0, copy, 0,
                         Math.min(original.length, newLength));
        return copy;
    }
```
区别：
arraycopy()需要目标数组，将原数组拷贝到你自己定义的数组里，而且可以选择拷贝的起点和长度以及放入新数组中的位置。

copyOf()是系统自动在内部新建一个数组，并返回该数组。

