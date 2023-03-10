package org.sirapi.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonFilter;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.sirapi.utils.SPARQLUtils;
import org.sirapi.utils.CollectionUtil;
import org.sirapi.utils.NameSpaces;
import org.sirapi.vocabularies.RDFS;
import org.sirapi.vocabularies.SIO;

@JsonFilter("entityFilter")
public class Entity extends HADatAcClass implements Comparable<Entity> {

    static String className = SIO.ENTITY;

    public List<Characteristic> characteristics;

    public Entity() {
        super(className);
        characteristics = new ArrayList<Characteristic>();
    }

    public static List<Entity> find() {
        List<Entity> entities = new ArrayList<Entity>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* <" + SIO.ENTITY + "> . " +
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.SPARQL_QUERY), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Entity entity = find(soln.getResource("uri").getURI());
            entities.add(entity);
            break;
        }

        java.util.Collections.sort((List<Entity>) entities);

        return entities;
    }

    public static Map<String,String> getMap() {
        List<Entity> list = find();
        Map<String,String> map = new HashMap<String,String>();
        for (Entity ent: list)
            map.put(ent.getUri(),ent.getLabel());
        return map;
    }

    public static List<String> getSubclasses(String uri) {
        List<String> subclasses = new ArrayList<String>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?uri WHERE { \n"
                + " ?uri rdfs:subClassOf* <" + uri + "> . \n"
                + " } \n";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.SPARQL_QUERY), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            subclasses.add(soln.get("uri").toString());
        }

        return subclasses;
    }

    public static Entity find(String uri) {
        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.SPARQL_QUERY), queryString);

        Entity entity = new Entity();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();
            RDFNode object = statement.getObject();
            if (statement.getPredicate().getURI().equals(RDFS.LABEL)) {
                String label = object.asLiteral().getString();

                // prefer longer one
                if (label.length() > entity.getLabel().length()) {
                    entity.setLabel(label);
                }
            } else if (statement.getPredicate().getURI().equals(RDFS.SUBCLASS_OF)) {
                entity.setSuperUri(object.asResource().getURI());
            }
        }

        entity.setUri(uri);
        entity.setLocalName(uri.substring(uri.indexOf('#') + 1));

        return entity;
    }

    @Override
    public int compareTo(Entity another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
}

