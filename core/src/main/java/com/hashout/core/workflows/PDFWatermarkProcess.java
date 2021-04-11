package com.hashout.core.workflows;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowData;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import com.day.cq.dam.api.Asset;
import com.day.cq.dam.api.AssetManager;
import com.hashout.core.utils.PDFHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.util.Matrix;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.adobe.cq.social.community.api.CommunityConstants.DAM_ROOT_PATH;
import static com.adobe.granite.workflow.PayloadMap.TYPE_JCR_PATH;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component(
        service = WorkflowProcess.class,
        property = {
                "process.label=PDF Watermark Process",
                Constants.SERVICE_DESCRIPTION + "=Adds a watermark based on the process arguments",
                Constants.SERVICE_VENDOR + "=Hashout Software Technologies"
        }
)
public class PDFWatermarkProcess implements WorkflowProcess {
    private static final Logger log = LoggerFactory.getLogger(PDFWatermarkProcess.class);
    private static final String TAG = PDFWatermarkProcess.class.toString();
    private static final String PDF_MIMETYPE = "application/pdf";

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) {

        final WorkflowData workflowData = workItem.getWorkflowData();
        final String type = workflowData.getPayloadType();
        final String payloadPath = workflowData.getPayload().toString();
        final ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
        final Resource assetResource = resolver != null ? resolver.getResource(payloadPath) : null;
        final AssetManager assetManager = resolver != null ? resolver.adaptTo(AssetManager.class) : null;

        // Check if the payload is under the DAM folder
        if (StringUtils.equals(type, TYPE_JCR_PATH)
                && StringUtils.startsWith(payloadPath, DAM_ROOT_PATH)
                && assetResource != null
                && assetManager != null) {
            //Adapt payload to asset
            final Asset asset = assetResource.adaptTo(Asset.class);
            if (asset != null && StringUtils.equals(asset.getMimeType(), PDF_MIMETYPE)) {
                ByteArrayInputStream watermarkedPDFStream = applyWatermark(asset, args);
                if (watermarkedPDFStream != null) {
                    assetManager.createAsset(payloadPath, watermarkedPDFStream, PDF_MIMETYPE, true);
                }
            }
        }

    }

    private ByteArrayInputStream applyWatermark(Asset pdfAsset, MetaDataMap args) {
        ByteArrayInputStream modifiedPDFStream = null;

        //Parse and Validate all the arguments
        final String text = args.get("text", String.class) != null ? args.get("text", String.class) : EMPTY;
        final PDFont font = PDFHelper.getFontFromName(args.get("font", String.class));
        final float fontSize = PDFHelper.getFontSize(args.get("size", String.class));
        final Color fontColor = PDFHelper.getColor(args.get("color", String.class));
        final float opacity = PDFHelper.getOpacity(args.get("opacity", String.class));
        final double orientation = PDFHelper.getOrientation(args.get("orientation", String.class));
        String position = args.get("position", String.class);

        try (final InputStream pdfStream = pdfAsset.getOriginal().getStream();
             final PDDocument doc = PDDocument.load(pdfStream)) {

            //These values are required to compute x and y offset for different positions
            float textWidth = font.getStringWidth(text) / 1000 * fontSize;
            float textHeight = font.getFontDescriptor().getFontBoundingBox().getHeight() / 1000 * fontSize;
            final float xOffset = PDFHelper.getXOffsetFromPosition(position, textWidth, doc);
            final float yOffset = PDFHelper.getYOffsetFromPosition(position, textHeight, doc);

            //Add bookmark on every page
            for (PDPage page : doc.getPages()) {
                PDPageContentStream cs =
                        new PDPageContentStream(
                                doc,
                                page,
                                PDPageContentStream.AppendMode.APPEND,
                                true,
                                true
                        );
                PDExtendedGraphicsState r0 = new PDExtendedGraphicsState();
                //Set Opacity/Transparency
                r0.setNonStrokingAlphaConstant(opacity);
                r0.setAlphaSourceFlag(true);

                cs.setGraphicsStateParameters(r0);

                //Set Font Color
                cs.setNonStrokingColor(fontColor);

                cs.beginText();

                //Set Font Type and Size
                cs.setFont(font, fontSize);

                // Position text according to the offsets calculated based on selected option
                cs.setTextMatrix(Matrix.getRotateInstance(orientation, xOffset, yOffset));

                //Set Actual Text
                cs.showText(text);
                cs.endText();

                cs.close();
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            doc.save(outputStream);
            doc.close();
            modifiedPDFStream = new ByteArrayInputStream(outputStream.toByteArray());
            return modifiedPDFStream;
        } catch (IOException e) {
            log.error("{}: Error while processing a PDF: {}", TAG, e.getMessage());
            return modifiedPDFStream;
        }
    }
}
