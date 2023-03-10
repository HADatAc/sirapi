package org.sirapi.console.controllers.restapi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.sirapi.entity.pojo.Detector;
import org.sirapi.utils.ApiUtil;
import org.sirapi.vocabularies.VSTOI;
import play.mvc.Controller;
import play.mvc.Result;
import static org.sirapi.Constants.*;
import java.util.List;


public class DetectorAPI extends Controller {

    private Result createDetectorResult(Detector detector) {
        detector.save();
        return ok(ApiUtil.createResponse("Detector <" + detector.getUri() + "> has been CREATED.", true));
    }

    public Result createDetectorsForTesting() {
        Detector testDetector1 = Detector.find(TEST_DETECTOR1_URI);
        Detector testDetector2 = Detector.find(TEST_DETECTOR2_URI);
        if (testDetector1 != null) {
            return ok(ApiUtil.createResponse("Test detector 1 already exists.", false));
        } else if (testDetector2 != null) {
            return ok(ApiUtil.createResponse("Test detector 2 already exists.", false));
        } else {
            testDetector1 = new Detector();
            testDetector1.setUri(TEST_DETECTOR1_URI);
            testDetector1.setLabel("Test Detector 1");
            testDetector1.setTypeUri(VSTOI.DETECTOR);
            testDetector1.setHascoTypeUri(VSTOI.DETECTOR);
            testDetector1.setComment("This is a dummy Detector 1 created to test the SIR API.");
            testDetector1.setHasContent("During the last 2 weeks, have you lost appetite?");
            testDetector1.setHasPriority("1");
            testDetector1.setIsInstrumentAttachment(TEST_INSTRUMENT_URI);
            testDetector1.save();
            testDetector2 = new Detector();
            testDetector2.setUri(TEST_DETECTOR2_URI);
            testDetector2.setLabel("Test Detector 2");
            testDetector2.setTypeUri(VSTOI.DETECTOR);
            testDetector2.setHascoTypeUri(VSTOI.DETECTOR);
            testDetector2.setComment("This is a dummy Detector 2 created to test the SIR API.");
            testDetector2.setIsInstrumentAttachment(TEST_INSTRUMENT_URI);
            testDetector1.setHasContent("During the last 2 weeks, have you gain appetite?");
            testDetector1.setHasPriority("2");
            testDetector2.save();
            return ok(ApiUtil.createResponse("Test Detectors 1 and 2 have been CREATED.", true));
        }
    }

    public Result createDetector(String json) {
        if (json == null || json.equals("")) {
            return ok(ApiUtil.createResponse("No json content has been provided.", false));
        }
        //System.out.println("Value of json: [" + json + "]");
        ObjectMapper objectMapper = new ObjectMapper();
        Detector newDetector;
        try {
            //convert json string to Instrument instance
            newDetector  = objectMapper.readValue(json, Detector.class);
        } catch (Exception e) {
            return ok(ApiUtil.createResponse("Failed to parse json.", false));
        }
        return createDetectorResult(newDetector);
    }

    private Result deleteDetectorResult(Detector detector) {
        String uri = detector.getUri();
        detector.delete();
        return ok(ApiUtil.createResponse("Detector <" + uri + "> has been DELETED.", true));
    }

    public Result deleteDetectorsForTesting(){
        Detector test1 = Detector.find(TEST_DETECTOR1_URI);
        Detector test2 = Detector.find(TEST_DETECTOR2_URI);
        if (test1 == null) {
            return ok(ApiUtil.createResponse("There is no Test Detector 1 to be deleted.", false));
        } else if (test2 == null) {
            return ok(ApiUtil.createResponse("There is no Test Detector 2 to be deleted.", false));
        } else {
            test1.delete();
            test2.delete();
            return ok(ApiUtil.createResponse("Test Detectors 1 and 2 have been DELETED.", true));
        }
    }

    public Result deleteDetector(String uri){
        if (uri == null || uri.equals("")) {
            return ok(ApiUtil.createResponse("No detector URI has been provided.", false));
        }
        Detector detector = Detector.find(uri);
        if (detector == null) {
            return ok(ApiUtil.createResponse("There is no detector with URI <" + uri + "> to be deleted.", false));
        } else {
            return deleteDetectorResult(detector);
        }
    }

    public Result getAllDetectors(){
        ObjectMapper mapper = new ObjectMapper();

        List<Detector> results = Detector.find();
        if (results == null) {
            return notFound(ApiUtil.createResponse("No detector has been found", false));
        } else {
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("detectorFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment", "isInstrumentAttachment", "hasContent", "hasPriority"));
            mapper.setFilterProvider(filterProvider);
            JsonNode jsonObject = mapper.convertValue(results, JsonNode.class);
            return ok(ApiUtil.createResponse(jsonObject, true));
        }
    }

}
