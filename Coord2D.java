/**
 * @author Alfonso Ridolfo
 * @version 1.5
 * 2-dimensional coordinates class
 */
public class Coord2D
{
	private  double  x, y;

	/**
	 * intialize a Coord2D object with x=0 and y=0
	 */
	Coord2D()
	{
		x = y = 0.0d;
	}

	/**
	 * intialize a Coord2D object with double values
	 * @param x abscissa
	 * @param y ordinate
	 */
	Coord2D(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	/**
	 * intialize a Coord2D object with int values
	 * @param x abscissa
	 * @param y ordinate
	 */
	Coord2D(int x, int y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * compute euclidean distance from the istance to pt
	 * @param pt
	 * @return a double value for euclidean distance
	 */
	public double computeEuclideanDistance(Coord2D pt)
	{
		double dx = x-pt.x;
		dx *= dx;
		double dy = y-pt.y;
		dy *= dy;
		return (double) Math.sqrt(dx+dy);
	}

	/**
	 * compute euclidean distance from the istance to a point (X,Y)
	 * @param X abscissa
	 * @param Y ordinate
	 * @return a double value for euclidean distance
	 */
	double computeEuclideanDistance(double X, double Y)
	{
		double dx = x-X;
		dx *= dx;
		double dy = y-Y;
		dy *= dy;
		return Math.sqrt(dx+dy);
	}
	
	/**
	 * @return abscissa
	 */
	public double X() {
		return this.x;
	}
	
	/**
	 * @return ordinate
	 */
	public double Y() {
		return this.y;
	}
}