package it.polito.tdp.extflightdelays.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private Map<Integer, Airport> airportIdMap;
	private ExtFlightDelaysDAO dao;
	private Map<Airport, Airport> alberoVisita;

	public Model() {
		airportIdMap = new HashMap<Integer, Airport>();
		dao = new ExtFlightDelaysDAO();
		dao.loadAllAirports(airportIdMap);
	}

	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		for (Airport a : airportIdMap.values()) {
			if (dao.getAirlinesNumber(a) > x) {
				grafo.addVertex(a);
			}
		}

		for (Rotta r : dao.getRotte(airportIdMap)) {
			if (grafo.containsVertex(r.getA1()) && grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e = grafo.getEdge(r.getA1(), r.getA2());
				if (e == null) {
					Graphs.addEdge(grafo, r.getA1(), r.getA2(), r.getPeso());
				} else {
					double pesoOld = grafo.getEdgeWeight(e);
					double pesoNew = pesoOld + r.getPeso();
					grafo.setEdgeWeight(e, pesoNew);
				}
			}
		}
	}

	public int verticiSize() {
		return grafo.vertexSet().size();
	}

	public int archiSize() {
		return grafo.edgeSet().size();
	}

	public Collection<Airport> getAirport() {
		return grafo.vertexSet();
	}

	public List<Airport> trovaPercorso(Airport a1, Airport a2) {
		List<Airport> result = new ArrayList<Airport>();
		alberoVisita = new HashMap<Airport, Airport>();
		alberoVisita.put(a1, null);
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<Airport, DefaultWeightedEdge>(
				grafo, a1);

		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport partenza = grafo.getEdgeSource(e.getEdge());
				Airport arrivo = grafo.getEdgeTarget(e.getEdge());

				if (!alberoVisita.containsKey(partenza) && alberoVisita.containsKey(arrivo)) {
					alberoVisita.put(partenza, arrivo);
				} else if (alberoVisita.containsKey(partenza) && !alberoVisita.containsKey(arrivo)) {
					alberoVisita.put(arrivo, partenza);
				}
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}
		});

		while (it.hasNext()) {
			it.next();
		}

		if (!alberoVisita.containsKey(a2)) {
			return null;
		}
		Airport step = a2;

		while (step != a1) {
			result.add(step);
			step = alberoVisita.get(step);
		}
		
		result.add(a1);

		return result;
	}

}
