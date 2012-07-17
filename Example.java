public class Example {
	
	public static void main (String args[]) {
		
		// Create an instance of Pickle
		Pickle p = new Pickle();
		
		// Connects to Club Penguin with the given information
		p.connect("username", "password", "Blizzard");
		
		// Checks if you are properly connected to Club Penguin
		if(p.isConnected()){
			
			// Joins the Ice Berg
			p.joinRoom(805);
			p.sleep(1);
			
			// Says "Hello world!" in the game
			p.sendMessage("Hello world!");
			p.sleep(2);
			
			// Throws a snowball to (400, 300)
			p.snowball(400, 300);
			p.sleep(2);
			
			// Says "Goodbye!" in the game
			p.sendMessage("Goodbye!");
			p.sleep(2);
			
			// Makes your penguin wave
			p.wave();
			p.sleep(2);
			
			// Disconnects from the server
			p.disconnect();

		}
		
		// If you didn't properly connect, this runs instead
		else{
			System.out.println("Could not connect to Club Penguin. Exiting.");
			System.exit(1);
		}
	}
}

