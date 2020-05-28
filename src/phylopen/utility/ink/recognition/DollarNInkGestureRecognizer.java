/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phylopen.utility.ink.recognition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import javafx.util.Pair;
import phylopen.utility.ink.InkStroke;

/**
 *
 * @author Work
 */
public class DollarNInkGestureRecognizer extends InkGestureRecognizer
{
    private final DollarNSymbolRecognizer dollarNRecognizer;
    private final TreeMap<String, List<List<InkStroke>>> trainingSymbols;

    public DollarNInkGestureRecognizer()
    {
        trainingSymbols = new TreeMap();
        dollarNRecognizer = new DollarNSymbolRecognizer();
    }

    @Override
    public InkGesture analyzeForGesture(Collection<AugmentedInkStroke> inkStrokes, double scale)
    {
        List<InkStroke> strokes = new ArrayList<>(inkStrokes);
        Pair<String, Double> result = dollarNRecognizer.recognizeSymbol(strokes, true, true);
        if (inkStrokes.size() > 1)
        {
            Pair<String, Double> result2a = dollarNRecognizer.recognizeSymbol(strokes.subList(0, 1), true, true);
            Pair<String, Double> result2b = dollarNRecognizer.recognizeSymbol(strokes.subList(1, 2), true, true);
            return analyzeForGesture(inkStrokes, scale, result.getKey(), result.getValue(), result2a.getKey(), result2a.getValue(), result2b.getKey(), result2b.getValue());
        }
        else
        {
            return analyzeForGesture(inkStrokes, scale, result.getKey(), result.getValue());
        }
    }

    public void clearTrainingSymbols()
    {
        trainingSymbols.clear();
        dollarNRecognizer.clearTrainingSet();
    }

    public void addToTrainingSymbols(String symbol, List<InkStroke> strokes)
    {
        List<List<InkStroke>> trainingSymbolStrokes;

        if (trainingSymbols.containsKey(symbol))
        {
            trainingSymbolStrokes = trainingSymbols.get(symbol);
        }
        else
        {
            trainingSymbolStrokes = new LinkedList<>();
            trainingSymbols.put(symbol, trainingSymbolStrokes);
        }

        trainingSymbolStrokes.add(strokes);

        if (!isDisabled(symbol))
            dollarNRecognizer.addToTrainingSet(symbol, strokes, true);
    }

    public boolean isDisabled(String symbol)
    {
        InkGestureRecognitionProcedure procedure = getProcedure(symbol);

        if (procedure != null)
            return procedure.isDisabled();

        return false;
    }

    public void setDisabled(String symbol, boolean value)
    {
        InkGestureRecognitionProcedure procedure = getProcedure(symbol);

        if (procedure != null)
        {
            if (procedure.isDisabled() != value)
            {
                if (procedure.isDisabled()) // add the symbols back in because enabling
                {
                    if (trainingSymbols.containsKey(symbol))
                    {
                        List<List<InkStroke>> symbolTrainingInstances = trainingSymbols.get(symbol);

                        for (List<InkStroke> symbolTrainingInstance : symbolTrainingInstances)
                            dollarNRecognizer.addToTrainingSet(symbol, symbolTrainingInstance, true);
                    }
                }
                else // removes symbols because disabling
                {
                    dollarNRecognizer.removeFromTrainingSet(symbol);
                }
            }

            procedure.setDisabled(value);
        }
    }

    private InkGestureRecognitionProcedure getProcedure(String symbol)
    {
        Iterator<InkGestureRecognitionProcedure> iterator = this.createGestureRecognitionProcedureIterator();
        InkGestureRecognitionProcedure procedure;

        while (iterator.hasNext())
        {
            procedure = iterator.next();

            if (procedure.getGestureIdentifier().equals(symbol))
                return procedure;
        }

        return null;
    }
}
