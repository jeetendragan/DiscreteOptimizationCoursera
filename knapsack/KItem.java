public class KItem{
	public double value;
	public double weight;
	public int indexInOrigin;
	public double ratio;
	
	
	public KItem(double value, double weight, int indexInOrigin){
		this.value = value;
		this.weight = weight;
		this.indexInOrigin = indexInOrigin;
		this.ratio = this.value / this.weight;
	}
}