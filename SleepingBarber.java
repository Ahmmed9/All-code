/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */


package com.mycompany.sleepingbarber;


/**
 *
 * @author abdulrhman
 */
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SleepingBarber {
	
	public static void main (String a[]) throws InterruptedException {	
		
		int NumOfBarbers=1, customerId=1, NumOfCustomers=15, NumOfChairs;	//initializing the number of barber and customers
		
		Scanner Obj = new Scanner(System.in);
		
		System.out.println("Enter the number of barbers :");			//input M barbers
    	NumOfBarbers=Obj.nextInt();
    	
    	System.out.println("Enter the number of Chairs :");			//input N waiting chairs:");
    	NumOfChairs=Obj.nextInt();
    	
//    	System.out.println("Enter the number of customers:");			//inout the number of customers for the shop
//    	noOfCustomers=sc.nextInt();
    	
		ExecutorService Threads = Executors.newFixedThreadPool(12);		//initializing with 12 threads as multiple of cores in the CPU, here 6
    	BarberShop shop = new BarberShop(NumOfBarbers, NumOfChairs);				//initializing the barber shop with the number of barbers
    	Random r = new Random();  										//a random number to calculate delays for customer arrivals and haircut
       	    	
        System.out.println("\nBarber shop opened with "
        		+NumOfBarbers+" barber(s)\n");
        
        long startTime  = System.currentTimeMillis();					//start time of program
        
        for(int i=1; i<=NumOfBarbers;i++) {								//generating the specified number of threads for barber
        	
        	Barber barber = new Barber(shop,i);	
        	Thread ThreadBarber = new Thread(barber);
            Threads.execute(ThreadBarber);
        }
        
        for(int i=0;i<NumOfCustomers;i++) {								//customer generator; generating customer threads
        
            Customer customer = new Customer(shop);
            customer.setInTime(new Date());
            Thread ThreadCustomer = new Thread(customer);
            customer.setcustomerId(customerId++);
            Threads.execute(ThreadCustomer);
            
            try {
            	
            	double val = r.nextGaussian() * 2000 + 2000;			//'r':object of Random class, nextGaussian() generates a number with mean 2000 and	
            	int millisDelay = Math.abs((int) Math.round(val));		//standard deviation as 2000, thus customers arrive at mean of 2000 milliseconds
            	Thread.sleep(millisDelay);								//and standard deviation of 2000 milliseconds
            }
            catch(InterruptedException iex) {
            
                iex.printStackTrace();
            }
            
        }
        
        Threads.shutdown();												//shuts down the executor service and frees all the resources
        Threads.awaitTermination(12, SECONDS);								//waits for 12 seconds until all the threads finish their execution
 
        long elapsedTime = System.currentTimeMillis() - startTime;		//to calculate the end time of program
        
        System.out.println("\nBarber shop closed");
        System.out.println("\nTotal time elapsed in seconds"
        		+ " for serving "+NumOfCustomers+" customers by "
        		+NumOfBarbers+" barbers with "+NumOfChairs+
        		" chairs in the waiting room is: "
        		+TimeUnit.MILLISECONDS
        	    .toSeconds(elapsedTime));
        System.out.println("\nTotal customers: "+NumOfCustomers+
        		"\nTotal customers served: "+shop.getTotalHairCuts()
        		+"\nTotal customers lost: "+shop.getCustomerLost());
               
        Obj.close();
    }
}
 
class Barber implements Runnable {										// initializing the barber

    BarberShop shop;
    int barberId;
 
    public Barber(BarberShop shop, int barberId) {
    
        this.shop = shop;
        this.barberId = barberId;
    }
    
    public void run() {
    
        while(true) {
        
            shop.cutHair(barberId);
        }
    }
}

class Customer implements Runnable {

    int customerId;
    Date inTime;
 
    BarberShop shop;
 
    public Customer(BarberShop shop) {
    
        this.shop = shop;
    }
 
    public int getCustomerId() {										//getter and setter methods
        return customerId;
    }
 
    public Date getInTime() {
        return inTime;
    }
 
    public void setcustomerId(int customerId) {
        this.customerId = customerId;
    }
 
    public void setInTime(Date inTime) {
        this.inTime = inTime;
    }
 
    public void run() {													//customer thread goes to the shop for the haircut
    
        goForHairCut();
    }
    private synchronized void goForHairCut() {							//customer is added to the list
    
        shop.add(this);
    }
}
 
class BarberShop {

	private final AtomicInteger totalHairCuts = new AtomicInteger(0);
	private final AtomicInteger customersLost = new AtomicInteger(0);
	int nchair, noOfBarbers, availableBarbers;
    List<Customer> listCustomer;
    
    Random r = new Random();	 
    
    public BarberShop(int noOfBarbers, int noOfChairs){
    
        this.nchair = noOfChairs;														//number of chairs in the waiting room
        listCustomer = new LinkedList<Customer>();						//list to store the arriving customers
        this.noOfBarbers = noOfBarbers;									//initializing the the total number of barbers
        availableBarbers = noOfBarbers;
    }
 
    public AtomicInteger getTotalHairCuts() {
    	
    	totalHairCuts.get();
    	return totalHairCuts;
    }
    
    public AtomicInteger getCustomerLost() {
    	
    	customersLost.get();
    	return customersLost;
    }
    
    public void cutHair(int barberId)
    {
        Customer customer;
        synchronized (listCustomer) {									//listCustomer is a shared resource so it has been synchronized to avoid any
        															 	//unexpected errors in the list when multiple threads access it
            while(listCustomer.size()==0) {
            
                System.out.println("\nBarber "+barberId+" is waiting "
                		+ "for the customer and sleeps in his chair");
                
                try {
                
                    listCustomer.wait();								//barber sleeps if there are no customers in the shop
                }
                catch(InterruptedException iex) {
                
                    iex.printStackTrace();
                }
            }
            
            customer = (Customer)((LinkedList<?>)listCustomer).poll();	//takes the first customer from the head of the list for haircut
            
            System.out.println("Customer "+customer.getCustomerId()+
            		" finds the barber asleep and wakes up "
            		+ "the barber "+barberId);
        }
        
        int millisDelay=0;
                
        try {
        	
        	availableBarbers--; 										//decreases the count of the available barbers as one of them starts 
        																//cutting hair of the customer and the customer sleeps
            System.out.println("Barber "+barberId+" cutting hair of "+
            		customer.getCustomerId()+ " so customer sleeps");
        	
            double val = r.nextGaussian() * 2000 + 4000;				//time taken to cut the customer's hair has a mean of 4000 milliseconds and
        	millisDelay = Math.abs((int) Math.round(val));				//and standard deviation of 2000 milliseconds
        	Thread.sleep(millisDelay);
        	
        	System.out.println("\nCompleted Cutting hair of "+
        			customer.getCustomerId()+" by barber " + 
        			barberId +" in "+millisDelay+ " milliseconds.");
        
        	totalHairCuts.incrementAndGet();
            															//exits through the door
            if(listCustomer.size()>0) {									
            	System.out.println("Barber "+barberId+					//barber finds a sleeping customer in the waiting room, wakes him up and
            			" wakes up a customer in the "					//and then goes to his chair and sleeps until a customer arrives
            			+ "waiting room");		
            }
            
            availableBarbers++;											//barber is available for haircut for the next customer
        }
        catch(InterruptedException iex) {
        
            iex.printStackTrace();
        }
        
    }
 
    public void add(Customer customer) {
    
        System.out.println("\nCustomer "+customer.getCustomerId()+
        		" enters through the entrance door in the the shop at "
        		+customer.getInTime());
 
        synchronized (listCustomer) {
        
            if(listCustomer.size() == nchair) {							//No chairs are available for the customer so the customer leaves the shop
            
                System.out.println("\nNo chair available "
                		+ "for customer "+customer.getCustomerId()+
                		" so customer leaves the shop");
                
              customersLost.incrementAndGet();
                
                return;
            }
            else if (availableBarbers > 0) {							//If barber is available then the customer wakes up the barber and sits in
            															//the chair
            	((LinkedList<Customer>)listCustomer).offer(customer);
				listCustomer.notify();
			}
            else {														//If barbers are busy and there are chairs in the waiting room then the customer
            															//sits on the chair in the waiting room
            	((LinkedList<Customer>)listCustomer).offer(customer);
                
            	System.out.println("All barber(s) are busy so "+
            			customer.getCustomerId()+
                		" takes a chair in the waiting room");
                 
                if(listCustomer.size()==1)
                    listCustomer.notify();
            }
        }
    }
}