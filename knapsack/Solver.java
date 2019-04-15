import java.io.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * The class <code>Solver</code> is an implementation of a greedy algorithm to solve the knapsack problem.
 *
 */
  
public class Solver {
    
    /**
     * The main class
     */
    public static void main(String[] args) {
        try {
            solve(args);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void solve(String[] args) throws IOException {
        String fileName = null;
        
        // get the temp file name - FOR SUBMISSION
        for(String arg : args){
            if(arg.startsWith("-file=")){
                fileName = arg.substring(6);
            }
        }
        
        if(fileName == null)
            return;
        
        // read the lines out of the file
        List<String> lines = new ArrayList<String>();

        BufferedReader input =  new BufferedReader(new FileReader(fileName));
        try {
            String line = null;
            while (( line = input.readLine()) != null){
                lines.add(line);
            }
        }
        finally {
            input.close();
        }
        
        // parse the data in the file
        String[] firstLine = lines.get(0).split("\\s+");
        int items = Integer.parseInt(firstLine[0]);
        int capacity = Integer.parseInt(firstLine[1]);
        
        List<Item> Items = new ArrayList<Item>(items);
        
        Item item;
        for(int i = 1; i <= items; i++){
          String line = lines.get(i);
          String[] parts = line.split("\\s+");
          
          item = new Item(i-1,Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
          
          Items.add(item);
          
        }
        
        /*for(int i = 0 ; i < items; i++){
        	Items.get(i).printData();
        }*/
        
        //int[][] values = new int[capacity+1][items+1];
        int[] taken = new int[items];
        
        double startTime = System.currentTimeMillis();
        
        int value = 0;
        ProblemState bestState = KnapsackSolver.Solve(Items, capacity);
        
        double endTime = System.currentTimeMillis();
        
        double runTime = (endTime - startTime)/1000;
        
        // System.out.println(runTime+" seconds");
        
        value = bestState.valueAttained;
        taken = bestState.taken;
        
        // prepare the solution in the specified output format DO NOT CHANGE THIS
        System.out.println(value+" 0");
        for(int i = 0; i < items; i++){
            System.out.print(taken[i]+" ");
        }
    }
}

class KnapsackSolver{
	public static ProblemState Solve(List<Item> Items, int capacity){
		
		List<Item> SortedItems = new ArrayList<Item>(Items.size());
		
		for(int i = 0 ; i < Items.size(); i++){
			SortedItems.add(Items.get(i));
		}
		// Sort the items by ratio, value/weight
		SortedItems.sort(new SortItemByRatio());
		
		/* System.out.println("After sorting the data:");
		for(int i = 0; i < Items.size(); i++){
			SortedItems.get(i).printData();
		}*/
		
		ProblemState currentState = new ProblemState(Items, SortedItems, capacity);
		ProblemState bestState = new ProblemState(Items, SortedItems, capacity);
		
		//System.out.println("Starting states: ");
		//System.out.println("Current State: ");
		//currentState.toString(0, "");
		//System.out.println("Best state: ");
		//bestState.toString(0, "");
		
		UseNext(0, "", Items, currentState, bestState);
		
		return bestState;
		
	}

	// returns the best value that has been attained as yet
	private static void UseNext(int itemIndex, String spaces, List<Item> items,
			ProblemState currentState,
			ProblemState bestState) {
		
		/*System.out.println(currentState.toString(itemIndex, spaces));
		System.out.println("*************Current best state**************");
		System.out.println(bestState.toString(itemIndex, spaces));
		System.out.println("________________Current best state ends here____________________");
		System.out.println("________________________________________________________________");*/
		
		// if the value attained in the best state is greater than 
		// the optimal value that can be attained in the current state
		// 	 	then prune the tree, i.e. return
		/*if(currentState.valueAttained < bestState.valueAttained){
			//System.out.println("currentState.optimisticEvaluation <= bestState.valueAttained");
			return; // Pruning the tree because a better solution will not be found if explored further
		}*/
		
		// check base case - is the knapsack full
		if(currentState.isMoreThanFull()){
			return;
		}
		
		// Check base case - we are at the bootom of the search tree. i.e. all items seen
		if(itemIndex == items.size()){
			// this is where we have found a complete solution.
			// Store the current state if it is better than the best state seen till now
			if(currentState.valueAttained > bestState.valueAttained){
				 currentState.copyStateTo(bestState);
			}
			return;
		}
		
		spaces +="  ";
		
		boolean itemConsidered = true;
		//currentState.consider(itemIndex, itemConsidered);
		currentState.MarkTaken(itemIndex, itemConsidered);
		currentState.MarkConsidered(itemIndex, itemConsidered);
		UseNext(itemIndex+1, spaces, items, currentState, bestState);
		currentState.MarkTakenBacktrack(itemIndex, itemConsidered);
		currentState.MarkConsideredBacktrack(itemIndex, itemConsidered);
		//currentState.backtrackConsider(itemIndex, itemConsidered);
		
		itemConsidered = false;
		currentState.MarkTaken(itemIndex, itemConsidered);
		currentState.MarkConsidered(itemIndex, itemConsidered);
		UseNext(itemIndex+1, spaces, items, currentState, bestState);
		currentState.MarkTakenBacktrack(itemIndex, itemConsidered);
		currentState.MarkConsideredBacktrack(itemIndex, itemConsidered);
	}
}

class ProblemState{
	
	int[] taken; // items put into the knapsack at any given state
	int[] itemsConsidered; // items that can be put into the knapsack at any given state
	List<Item> Items; 
	List<Item> SortedItems;
	int KnapsackCapacity;
	int currentWeight;
	int valueAttained;
	
	double optimisticEvaluation; // the best value that can be attained if we 
	// consider all the items in itemsConsiderd
	
	ProblemState(List<Item> Items, List<Item> SortedItems, int KnapsackCapacity){
		taken = new int[Items.size()];
		itemsConsidered = new int[Items.size()];
		for(int i = 0 ; i < itemsConsidered.length; i++){
			itemsConsidered[i] = 1; // Initially all the items will be considered for optimistic eval
		}
		this.SortedItems = SortedItems;
		this.Items = Items;
		this.KnapsackCapacity = KnapsackCapacity;
		this.CalculateOptimisticEvaluation();
	}

	public void MarkConsideredBacktrack(int itemIndex, boolean itemConsidered) {
		if(!itemConsidered){
			itemsConsidered[itemIndex] = 1;
			CalculateOptimisticEvaluation();
		}//else{ no need to calculate the opEval as the value has not changed}
	}

	public void MarkTakenBacktrack(int itemIndex, boolean itemConsidered) {
		if(itemConsidered){
			taken[itemIndex] = 0;
			currentWeight -= Items.get(itemIndex).weight;
			valueAttained -= Items.get(itemIndex).value;
		}
		// else{ No need to backtrack, as no changes are made to currentWeight and valueAttained}
	}

	public void MarkConsidered(int itemIndex, boolean itemConsidered) {
		if(this.itemsConsidered[itemIndex] == 1 && itemConsidered)
		{
			// no need to recompute the optimistic evaluation - Proof
			return;
		}
		if(this.itemsConsidered[itemIndex] == 0 && !itemConsidered){
			// this is the same as the previous state
			return;
		}
		
		if(itemConsidered){
			this.itemsConsidered[itemIndex] = 1;
		}else{
			this.itemsConsidered[itemIndex] = 0;
		}
		//CalculateOptimisticEvaluation();
	}

	public void MarkTaken(int itemIndex, boolean itemConsidered) {
		if(itemConsidered){
			this.taken[itemIndex] = 1;
			this.currentWeight += Items.get(itemIndex).weight;
			this.valueAttained += Items.get(itemIndex).value;
		}else{
			this.taken[itemIndex] = 0;
		}
	}

	// If true marks the item at the itemIndex as considered
	public void consider(int itemIndex, boolean toBeConsidered) {
		if(toBeConsidered){
			taken[itemIndex] = 1;
			itemsConsidered[itemIndex] = 1;
			currentWeight += Items.get(itemIndex).weight;
			valueAttained += Items.get(itemIndex).value;
		}else{
			itemsConsidered[itemIndex] = 0;
			taken[itemIndex] = 0;
			CalculateOptimisticEvaluation();
		}
	}
	
	public void backtrackConsider(int itemIndex, boolean itemAtIndexConsidered) {
		if(itemAtIndexConsidered){
			taken[itemIndex] = 0;
			currentWeight -= Items.get(itemIndex).weight;
			valueAttained -= Items.get(itemIndex).value;
		}else{
			itemsConsidered[itemIndex] = 1;
		}
	}

	private void CalculateOptimisticEvaluation() {
		Item item;
		int weight = 0, left;
		double opEvaluation = 0, ratio;
	
		for(int i = 0; i < SortedItems.size(); i++){
			item = SortedItems.get(i);
			if(itemsConsidered[item.Id] == 0) 
				continue; // the item has not been considered
			
			// if the item has been considerd for optimistic evaluation
			if(weight + item.weight > KnapsackCapacity){
				// if the current item cannot fit into the knapsack - use a fraction of it
				left = KnapsackCapacity - weight; // what's left is always going to be less than item.weight
				ratio = left / (double)item.weight;
				opEvaluation += ratio * item.value;
				weight += ratio * item.weight;
				break;
			}else{
				// Complete item can be put into the knapsack
				weight += item.weight;
				opEvaluation += item.value;
			}
		}
		this.optimisticEvaluation = opEvaluation;
	}
	
	public boolean isMoreThanFull(){
		return currentWeight > KnapsackCapacity;
	}
	
	public ProblemState copyStateTo(ProblemState state){
		
		for(int i = 0 ; i < this.taken.length; i++){
			state.taken[i] = this.taken[i];
		}
		
		state.currentWeight = this.currentWeight;
		state.valueAttained = this.valueAttained;
		state.optimisticEvaluation = this.optimisticEvaluation;
		
		return state;
	}
	
	public String toString(int itemIndex, String spaces){
		String str = spaces+"Items taken: ";
		
		for(int i = 0; i < taken.length ; i++){
			if(i == itemIndex -1)
				str += "("+taken[i]+")";
			else
				str += taken[i]+" ";
		}
		str +="\n";
		str += spaces+"Items considered: ";
		
		for(int i = 0; i < itemsConsidered.length ; i++){
			if(i == itemIndex -1)
				str += "("+itemsConsidered[i]+")";
			else
				str += itemsConsidered[i]+" ";
		}
		
		str += "\n";
		str += spaces+"Weight: "+currentWeight+"\n";
		str += spaces+"Value: "+valueAttained+"\n";
		str += spaces+"OpEvaluation +"+optimisticEvaluation+"\n";
		return str;
	}
	
	
}
