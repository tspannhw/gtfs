/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.datainmotion.gtfs;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.xml.sax.SAXException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Tags({"gtfs,feeds,real-time gtfs,protocol buffers,streaming,data ingest, rest api"})
@CapabilityDescription("Uses Google GTFS Java library to read and translate GTFS feeds")
@SeeAlso({})
@ReadsAttributes({@ReadsAttribute(attribute="GTFS_URL", description="URL for GTFS Feed")})
@WritesAttributes({@WritesAttribute(attribute="status", description="Status result")})
public class GTFSRealTimeProcessor extends AbstractProcessor {

    private GTFSRealTimeService gtfsRealTimeService = null;

    public static String GTFS_URL_NAME = "GTFS_URL";

    public static final PropertyDescriptor GTFS_URL = new PropertyDescriptor
            .Builder().name(GTFS_URL_NAME)
            .displayName("GTFS URL")
            .description("GTFS URL")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder().name("success")
            .description("Successfully loaded GTFS.").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("Failed to load GTFS.").build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(GTFS_URL);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        gtfsRealTimeService = new GTFSRealTimeService();
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        if (session == null || context == null) {
            return;
        }
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            flowFile = session.create();
        }
        try {
            flowFile.getAttributes();

            if (gtfsRealTimeService == null) {
                gtfsRealTimeService = new GTFSRealTimeService();
            }
            String gtfsURL = flowFile.getAttribute(GTFS_URL_NAME);
            if (gtfsURL == null) {
                gtfsURL = context.getProperty(GTFS_URL_NAME).evaluateAttributeExpressions(flowFile).getValue();
            }
            final String gtfsStringURL = gtfsURL;
            final HashMap<String, String> attributes = new HashMap<String, String>();
            attributes.put("gtfs_url", gtfsStringURL);

            boolean isValid = false;

            try {
                List<Result> results = gtfsRealTimeService.listData(gtfsStringURL);

                int counter = 1;

                if (results != null && results.size() > 0) {
                    for (Result gtfsEntry : results) {
                        if ( gtfsEntry != null ) {
                            attributes.put("gtfs.id." + counter, gtfsEntry.getId());

                            if (gtfsEntry.getAlert()!= null) {
                                attributes.put("gtfs.alert." + counter, gtfsEntry.getAlert().toString());
                            }

                            if (gtfsEntry.getTripUpdate() != null ) {
                                attributes.put("gtfs.tripupdate." + counter, gtfsEntry.getTripUpdate().toString());
                            }

                            if (gtfsEntry.getVehicle() != null ) {
                                attributes.put("gtfs.vehicle." + counter, gtfsEntry.getVehicle().toString());
                            }

                            if (gtfsEntry.getStatusCode() != gtfsRealTimeService.STATUS_CODE_GOOD) {
                                isValid = false;
                            }
                            attributes.put("gtfs.isdeleted."+ counter, Boolean.toString(gtfsEntry.isDeleted()));

                            counter++;
                        }
                    }

                    isValid = true;
                }
            } catch (Exception x) {
                x.printStackTrace();
                isValid = false;
            }
            if (isValid) {
                attributes.put("gtfs.success", "true");

                flowFile = session.putAllAttributes(flowFile, attributes);

                session.transfer(flowFile, REL_SUCCESS);
            } else {
                attributes.put("gtfs.success", "false");
                flowFile = session.putAllAttributes(flowFile, attributes);
                session.transfer(flowFile, REL_FAILURE);
            }

//            try {
//                final AtomicReference<String> type = new AtomicReference<>();
//                final AtomicReference<Boolean> wasError = new AtomicReference<>(false);
//
//                flowFile = session.write(flowFile, new StreamCallback() {
//                    @Override
//                    public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
//                        if (inputStream != null ) {
//                            BufferedInputStream buffStream = new BufferedInputStream(inputStream);
//                            Tika tika = new Tika();
//                            String text = "";
//                            try {
//                                type.set(tika.detect(buffStream, filename));
//
//                                if (outputMode.equals(HTML_FORMAT)) {
//                                    // http://lifeinide.com/post/2013-10-18-convert-document-to-html-with-apache-tika/
//
//                                    ByteArrayOutputStream out = new ByteArrayOutputStream();
//                                    SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
//                                    TransformerHandler handler = factory.newTransformerHandler();
//                                    handler.getTransformer().setOutputProperty(OutputKeys.METHOD, "html");
//                                    handler.getTransformer().setOutputProperty(OutputKeys.INDENT, "yes");
//                                    handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, "UTF-8");
//                                    handler.setResult(new StreamResult(out));
//                                    ExpandedTitleContentHandler handler1 = new ExpandedTitleContentHandler(handler);
//
//                                    AutoDetectParser parser = new AutoDetectParser();
//                                    parser.parse(buffStream, handler1, new Metadata());
//                                    text = new String(out.toByteArray(), "UTF-8");
//                                } else {
//                                    tika.setMaxStringLength(maxTextLength);
//                                    text = tika.parseToString(buffStream);
//                                }
//
//                            } catch (TikaException e) {
//                                getLogger().error("Apache Tika failed to parse input " + e.getLocalizedMessage());
//                                wasError.set(true);
//                            } catch (SAXException e) {
//                                getLogger().error(
//                                        "Apache Tika failed to parse input on XML/HTML error " + e.getLocalizedMessage());
//                                wasError.set(true);
//                            } catch (TransformerConfigurationException e) {
//                                getLogger().error(
//                                        "Apache Tika failed to parse input on XML/HTML error " + e.getLocalizedMessage());
//                                wasError.set(true);
//                            }
//
//                            outputStream.write(text.getBytes());
//                            buffStream.close();
//                        }
//                        else {
//                            getLogger().error("Input file was null");
//                            wasError.set(true);
//                        }
//                    }
//                });
//
//                if (wasError.get()) {
//                    session.transfer(flowFile, REL_FAILURE);
//                } else {
//
//                    Map<String, String> mimeAttrs = null;
//
//                    if (outputMode.equals(HTML_FORMAT)) {
//                        mimeAttrs = new HashMap<String, String>() {
//                            {
//                                put("mime.type", TEXT_HTML);
//                                put("orig.mime.type", type.get());
//                            }
//                        };
//                    } else {
//                        mimeAttrs = new HashMap<String, String>() {
//                            {
//                                put("mime.type", TEXT_PLAIN);
//                                put("orig.mime.type", type.get());
//                            }
//                        };
//                    }
//
//                    flowFile = session.putAllAttributes(flowFile, mimeAttrs);
//                    session.transfer(flowFile, REL_SUCCESS);
//                }
//                session.commit();
            session.commit();
        } catch (final Throwable t) {
            t.printStackTrace();
            getLogger().error("Unable to read GTFS Data: " + t.getLocalizedMessage());
            throw new ProcessException(t);
        }
    }
}
