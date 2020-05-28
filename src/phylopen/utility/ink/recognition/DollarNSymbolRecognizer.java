/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.util.Pair;
import phylopen.utility.ink.InkStroke;
import phylopen.utility.ink.InkUtility;
import phylopen.utility.ink.StylusPoint;

/**
 *
 * @author awehrer
 */
public class DollarNSymbolRecognizer
{
    private final List<Multistroke> trainingSet;
    
    // constants
    private final int NUM_RESAMPLE_POINTS = 96;
    private final int SQUARE_SIZE = 250;
    private final double RATIO_THRESHOLD = 0.30;
    private final Point2D ORIGIN = new Point2D(0.0, 0.0);
    private final int START_ANGLE_INDEX = 12;
    private final double ANGLE_SIMILARITY_THRESHOLD = 30.0 * (Math.PI / 180.0); // 30 degrees converted to radians
    private final double ANGLE_RANGE = 45.0 * (Math.PI / 180.0);
    private final double ANGLE_PRECISION = 2.0 * (Math.PI / 180.0);

    public DollarNSymbolRecognizer()
    {
        trainingSet = new ArrayList<>();
    }
    
    public void addToTrainingSet(String symbolName, List<InkStroke> inkStrokes)
    {
        addToTrainingSet(symbolName, inkStrokes, false);
    }

    public void addToTrainingSet(String symbolName, List<InkStroke> inkStrokes, boolean boundedRotationInvariance)
    {
        trainingSet.add(new Multistroke(symbolName, inkStrokes, boundedRotationInvariance));
    }
    
    public void removeFromTrainingSet(String symbolName)
    {
        Iterator<Multistroke> iterator = trainingSet.iterator();
        Multistroke trainingSymbol;
        
        while (iterator.hasNext())
        {
            trainingSymbol = iterator.next();
            
            if (trainingSymbol.getName().equals(symbolName))
                iterator.remove();
        }
    }

    public void clearTrainingSet()
    {
        trainingSet.clear();
    }

    // combine candidate strokes into one unistroke points path
    private List<StylusPoint> combineStrokePoints(List<InkStroke> strokes)
    {
        List<StylusPoint> points = new ArrayList<>();
        List<StylusPoint> pointCollectionClone;

        for (InkStroke stroke : strokes)
        {
            pointCollectionClone = new ArrayList<>(stroke.getStylusPoints());
            
            for (StylusPoint point : pointCollectionClone)
                points.add(point);
        }

        return points;
    }
    
    /*public Pair<String[], BoundingBox[]> recognizeString(List<InkStroke> strokes)
    {
        return recognizeString(strokes, false, false);
    }

    public Pair<String[], BoundingBox[]> recognizeString(List<InkStroke> strokes, boolean requireSameNumStrokes, boolean boundedRotationInvariance)
    {
        String recognizedSymbol, previousSymbol = null;
        BoundingBox symbolBounds, previousBounds = new BoundingBox(0.0, 0.0, -1.0, -1.0); // EMPTY value

        Segmentation segmentation = new Segmentation();
        List<StrokeCollection> strokeSegments = segmentation.GroupByIntersection(strokes, 30);

        List<string> symbols = new List<string>();
        List<Rect> bounds = new List<Rect>();

        foreach (StrokeCollection segment in strokeSegments)
        {
            Tuple<string, double> result = DollarNSymbolRecognizer.this.recognizeSymbol(segment, requireSameNumStrokes, boundedRotationInvariance);
            recognizedSymbol = result.Item1;
            symbolBounds = segment.GetBounds();

            // special case: equal sign
            if (previousSymbol != null && previousSymbol.Equals("-") && recognizedSymbol.Equals("-"))
            {
                Rect temp = new Rect(previousBounds.X, symbolBounds.Y, previousBounds.Width, previousBounds.Height);

                if (!Rect.Intersect(symbolBounds, temp).Equals(Rect.Empty))
                {
                    recognizedSymbol = "=";
                    //Console.WriteLine(symbolBounds.X + ", " + symbolBounds.Y + ", " + symbolBounds.Width + ", " + symbolBounds.Height);
                    //PrintBounds(symbolBounds);
                    //PrintBounds(previousBounds);
                    symbolBounds = Rect.Union(symbolBounds, previousBounds);
                    bounds.RemoveAt(bounds.Count() - 1);
                    symbols.RemoveAt(symbols.Count() - 1);
                    //PrintBounds(symbolBounds);
                }
            }

            //Console.WriteLine(recognizedSymbol);
            symbols.Add(recognizedSymbol);
            bounds.Add(symbolBounds);
            previousSymbol = recognizedSymbol;
            previousBounds = symbolBounds;
        }

        return new Pair<String[], BoundingBox[]>(symbols.ToArray(), bounds.ToArray());
    }*/
    
    public Pair<String, Double> recognizeSymbol(List<InkStroke> strokes)
    {
        return recognizeSymbol(strokes, false, false);
    }

    public Pair<String, Double> recognizeSymbol(List<InkStroke> strokes, boolean requireSameNumStrokes, boolean boundedRotationInvariance)
    {
        // combine candidate stroke into one unistroke points path
        List<StylusPoint> points = combineStrokePoints(strokes);

        // process points
        points = InkUtility.resample(points, NUM_RESAMPLE_POINTS);
        double indicativeAngle = InkUtility.computeIndicativeAngle((List)points);
        points = InkUtility.rotateBy(points, -indicativeAngle);
        points = InkUtility.scaleDimTo(points, SQUARE_SIZE, RATIO_THRESHOLD);

        if (boundedRotationInvariance)
            points = InkUtility.rotateBy(points, indicativeAngle);

        points = InkUtility.translateTo(points, ORIGIN);
        Point2D startUnitVector = InkUtility.calcStartUnitVector(points, START_ANGLE_INDEX);

        // begin recognition
        double leastDistance = Double.POSITIVE_INFINITY;
        Multistroke owner = null;
        double score = 0.0;

        // for each template multistroke
        for (Multistroke template : trainingSet)
        {
            // if not require same number of strokes or same number of strokes
            if (!requireSameNumStrokes || strokes.size() == template.getNumStrokes())
            {
                for (Unistroke unistroke : template.getUnistrokes())
                {
                    if ((angleBetweenVectors(startUnitVector, unistroke.getStartUnitVector()) <= ANGLE_SIMILARITY_THRESHOLD))
                    {
                        double distance = distanceAtBestAngle(points, unistroke, -ANGLE_RANGE, ANGLE_RANGE, ANGLE_PRECISION);
                        
                        //System.out.println(template.getName() + " -> " + distance);
                        
                        if (distance < leastDistance)
                        {
                            leastDistance = distance;
                            owner = template;
                        }
                    }
                }
            }
        }

        if (owner == null)
            return new Pair<>("?", score);

        score = 1.0 - leastDistance / (0.5 * Math.sqrt(SQUARE_SIZE * SQUARE_SIZE + SQUARE_SIZE * SQUARE_SIZE));
        return new Pair<>(owner.getName(), score);
    }
    
    public List<Pair<String, Double>> getSymbolCandidates(List<InkStroke> strokes)
    {
        return getSymbolCandidates(strokes, false, false);
    }

    public List<Pair<String, Double>> getSymbolCandidates(List<InkStroke> strokes, boolean requireSameNumStrokes, boolean boundedRotationInvariance)
    {
        // combine candidate stroke into one unistroke points path
        List<StylusPoint> points = combineStrokePoints(strokes);
        List<Pair<String, Double>> symbolCandidates = new ArrayList<>();

        // process points
        points = InkUtility.resample(points, NUM_RESAMPLE_POINTS);
        double indicativeAngle = InkUtility.computeIndicativeAngle((List)points);
        points = InkUtility.rotateBy(points, -indicativeAngle);
        points = InkUtility.scaleDimTo(points, SQUARE_SIZE, RATIO_THRESHOLD);

        if (boundedRotationInvariance)
            points = InkUtility.rotateBy(points, indicativeAngle);

        points = InkUtility.translateTo(points, ORIGIN);
        Point2D startUnitVector = InkUtility.calcStartUnitVector(points, START_ANGLE_INDEX);

        // begin matching

        // for each template multistroke
        for (Multistroke template : trainingSet)
        {
            // if not require same number of strokes or same number of strokes
            if (!requireSameNumStrokes || strokes.size() == template.getNumStrokes())
            {
                double leastDistance = Double.POSITIVE_INFINITY;

                // get the best unistroke permutation for each template
                for (Unistroke unistroke : template.getUnistrokes())
                {
                    if (angleBetweenVectors(startUnitVector, unistroke.getStartUnitVector()) <= ANGLE_SIMILARITY_THRESHOLD)
                    {
                        double distance = distanceAtBestAngle(points, unistroke, -ANGLE_RANGE, ANGLE_RANGE, ANGLE_PRECISION);
                        
                        //System.out.println(template.getName() + " -> " + distance);
                        
                        if (distance < leastDistance)
                        {
                            leastDistance = distance;
                        }
                    }
                }

                // add best unistroke permutation score, if any
                if (leastDistance != Double.POSITIVE_INFINITY)
                {
                    insertIntoSortedSymbolCandidates(template.getName(), calculateScore(leastDistance, SQUARE_SIZE), symbolCandidates);
                }
            }
        }

        if (symbolCandidates.isEmpty())
            symbolCandidates.add(new Pair<>("?", 0.0));

        return symbolCandidates;
    }

    private double firstToLastPointDistance(List<StylusPoint> points)
    {
        return InkUtility.distance(points.get(0), points.get(points.size() - 1));
    }

    private void insertIntoSortedSymbolCandidates(String symbol, double score, List<Pair<String, Double>> symbolCandidates)
    {
        int i;

        for (i = 0; i < symbolCandidates.size(); i++)
        {
            if (symbolCandidates.get(i).getValue() > score)
            {
                symbolCandidates.add(i, new Pair<>(symbol, score));
                break;
            }
        }

        if (i == symbolCandidates.size())
            symbolCandidates.add(new Pair<>(symbol, score));
    }

    private double calculateScore(double leastDistance, double squareSize)
    {
        return 1.0 - leastDistance / (0.5 * Math.sqrt(squareSize * squareSize + squareSize * squareSize));
    }

    private double angleBetweenVectors(Point2D a, Point2D b)
    {
        return Math.acos(a.getX() * b.getX() + a.getY() * b.getY());
    }

    private double distanceAtBestAngle(List<StylusPoint> points, Unistroke unistroke, double angleRangeLeft, double angleRangeRight, double threshold)
    {
        double phi = 0.5 * (-1.0 + Math.sqrt(5.0)); // Golden Ratio
        double x1 = phi * angleRangeLeft + (1.0 - phi) * angleRangeRight;
        double f1 = distanceAtAngle(points, unistroke, x1);
        double x2 = (1.0 - phi) * angleRangeLeft + phi * angleRangeRight;
        double f2 = distanceAtAngle(points, unistroke, x2);

        while (Math.abs(angleRangeRight - angleRangeLeft) > threshold)
        {
            if (f1 < f2)
            {
                angleRangeRight = x2;
                x2 = x1;
                f2 = f1;
                x1 = phi * angleRangeLeft + (1.0 - phi) * angleRangeRight;
                f1 = distanceAtAngle(points, unistroke, x1);
            }
            else
            {
                angleRangeLeft = x1;
                x1 = x2;
                f1 = f2;
                x2 = (1.0 - phi) * angleRangeLeft + phi * angleRangeRight;
                f2 = distanceAtAngle(points, unistroke, x2);
            }
        }

        return Math.min(f1, f2);
    }

    private double distanceAtAngle(List<StylusPoint> points, Unistroke unistroke, double radians)
    {
        List<StylusPoint> rotatedPoints = InkUtility.rotateBy(points, radians);
        return averageDistanceBetweenCorrespondingPoints(rotatedPoints, unistroke.getPoints());
    }

    private double averageDistanceBetweenCorrespondingPoints(List<StylusPoint> points1, List<StylusPoint> points2)
    {
        double distance = 0.0;

        for (int i = 0; i < points1.size(); i++)
            distance += InkUtility.distance(points1.get(i), points2.get(i));

        return distance / points1.size();
    }
    
    public class Unistroke
    {
        private List<StylusPoint> points;
        private Point2D startUnitVector;

        public Unistroke(InkStroke stroke, boolean boundedRotationInvariance)
        {
            this(new ArrayList<>(stroke.getStylusPoints()), boundedRotationInvariance);
        }

        public Unistroke(List<StylusPoint> points, boolean boundedRotationInvariance)
        {
            this.points = InkUtility.resample(points, NUM_RESAMPLE_POINTS);
            double indicativeAngle = InkUtility.computeIndicativeAngle((List)this.points);
            this.points = InkUtility.rotateBy(this.points, -indicativeAngle);
            this.points = InkUtility.scaleDimTo(this.points, SQUARE_SIZE, RATIO_THRESHOLD);

            if (boundedRotationInvariance)
                this.points = InkUtility.rotateBy(this.points, indicativeAngle);

            this.points = InkUtility.translateTo(this.points, ORIGIN);
            this.startUnitVector = InkUtility.calcStartUnitVector(this.points, START_ANGLE_INDEX);
        }

        public Point2D getStartUnitVector()
        {
            return startUnitVector;
        }

        public List<StylusPoint> getPoints()
        {
            return points;
        }
    }
    
    public class Multistroke
    {
        private final String name;
        private final List<Unistroke> unistrokes;
        private final int numStrokes;

        public Multistroke(String name, List<InkStroke> inkStrokes, boolean boundedRotationInvariance)
        {
            this.name = name;
            this.unistrokes = new ArrayList<>();
            this.numStrokes = inkStrokes.size();
            generateUnistrokePermutations(inkStrokes, boundedRotationInvariance);
        }

        public String getName()
        {
            return name;
        }

        public int getNumStrokes()
        {
            return numStrokes;
        }

        public List<Unistroke> getUnistrokes()
        {
            return unistrokes;
        }

        private void generateUnistrokePermutations(List<InkStroke> strokes, boolean boundedRotationInvariance)
        {
            int[] order = new int[strokes.size()]; // default permutation
            List<int[]> orders = new ArrayList<>(); // holds permutations

            // initialize default permutation
            for (int i = 0; i < strokes.size(); i++)
                order[i] = i;

            heapPermute(strokes.size(), order, orders);

            List<InkStroke> unistrokeList = makeUnistrokes(strokes, orders);

            for (InkStroke unistroke : unistrokeList)
                this.unistrokes.add(new Unistroke(unistroke, boundedRotationInvariance));
        }

        private void swap(int index1, int index2, int[] array)
        {
            int temp = array[index1];
            array[index1] = array[index2];
            array[index2] = temp;
        }

        private void heapPermute(int n, int[] order, List<int[]> orders)
        {
            if (n == 1)
            {
                int[] orderCopy = Arrays.copyOf(order, order.length);
                orders.add(orderCopy);
            }
            else
            {
                for (int i = 0; i < n; i++)
                {
                    heapPermute(n - 1, order, orders);

                    // if n is odd
                    if (n % 2 == 1)
                        swap(0, n - 1, order);
                    else
                        swap(i, n - 1, order);
                }
            }
        }

        private List<InkStroke> makeUnistrokes(List<InkStroke> strokes, List<int[]> orders)
        {
            List<StylusPoint> newPoints;
            List<StylusPoint> newPointCollection;
            List<StylusPoint> unistrokePoints;
            List<InkStroke> unistrokeList = new ArrayList<>();

            // for each stroke order permutation
            for (int[] order : orders)
            {
                // b used for bits
                for (int b = 0; b < (int)Math.pow(2, order.length); b++)
                {
                    unistrokePoints = new ArrayList<>();

                    // for each stroke in the order
                    for (int i = 0; i < order.length; i++)
                    {
                        // if bit at index i is 1
                        if (((b >> i) & 1) == 1)
                        {
                            // copy, reverse
                            newPoints = new ArrayList<>(strokes.get(order[i]).getStylusPoints());
                            Collections.reverse(newPoints);
                            newPointCollection = new ArrayList<>(newPoints);
                        }
                        else
                        {
                            // copy
                            newPointCollection = new ArrayList<>(strokes.get(order[i]).getStylusPoints());
                        }

                        // append stroke points to unistroke
                        unistrokePoints.addAll(newPointCollection);
                    }

                    // finished constructing unistroke; append it to list of unistrokes
                    unistrokeList.add(new InkStroke(unistrokePoints));
                }
            }

            return unistrokeList;
        }
    }
}
