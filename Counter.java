public class Counter {
   private int count;
   public Counter() {
      count = 1;
   }
   public void setCount(int n) {
      count = n;
   }
   public int getCount() {
      return count;
   }
   
   public void increment() {
      count++;
   }
}
