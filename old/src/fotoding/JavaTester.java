/**
 * Created on 12.05.2003
 * @author hirsch
 *
 */
package fotoding;

public class JavaTester  {
private String name;
	public JavaTester(String name) {
		this.name=name;
	}// end of Konstruktor 
	
	public String getUser() {
		return this.name;
	} // end of getUser


	public static void main(String[] args) {
		JavaTester jt= new JavaTester("Otti");
		
		System.out.println("JavaTester = "+jt.getUser());
		
//		statisch: this.setDefaultLookAndFeelDecorated(true);
		GrafikZeug gf = new GrafikZeug();

	} /* end of main */
} /* end of JavaTester */
