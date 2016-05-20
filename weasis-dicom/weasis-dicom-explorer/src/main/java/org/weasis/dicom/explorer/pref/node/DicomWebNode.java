package org.weasis.dicom.explorer.pref.node;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.weasis.core.api.util.StringUtil;
import org.weasis.dicom.codec.TransferSyntax;

public class DicomWebNode extends AbstractDicomNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(DicomWebNode.class);

    private static final String T_URL = "url";
    private static final String T_WEB_TYPE = "webtype";

    public enum WebType {
        WADO("WADO"), WADORS("WADO-RS"), STOWRS("STOW-RS");

        String title;

        WebType(String title) {
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }

        public static WebType getWebType(String name) {
            try {
                return WebType.valueOf(name);
            } catch (Exception e) {
                // DO nothing
            }
            return WADO;
        }
    }

    // For WADO, WADO-RS and STOW
    private URL url;
    private WebType webType;

    public DicomWebNode(String description, WebType webType, URL url) {
        super(description, Type.WEB);
        this.url = url;
        this.webType = webType;
    }

    @Override
    public String getToolTips() {
        StringBuilder toolTips = new StringBuilder();
        toolTips.append("<html>"); //$NON-NLS-1$
        toolTips.append(toString());
        toolTips.append("<br>"); //$NON-NLS-1$
        toolTips.append(webType.toString());
        toolTips.append(StringUtil.COLON_AND_SPACE);
        toolTips.append(url);
        toolTips.append("</html>"); //$NON-NLS-1$
        return toolTips.toString();
    }

    public WebType getWebType() {
        return webType;
    }

    public void setWebType(WebType webType) {
        this.webType = webType;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @Override
    public void saveDicomNode(XMLStreamWriter writer) throws XMLStreamException {
        super.saveDicomNode(writer);
        writer.writeAttribute(T_URL, url.toString());
        writer.writeAttribute(T_WEB_TYPE, StringUtil.getEmpty2NullEnum(webType));
    }

    public static DicomWebNode buildDicomWebNode(XMLStreamReader xmler) throws MalformedURLException {
        DicomWebNode node = new DicomWebNode(xmler.getAttributeValue(null, T_DESCRIPTION),
            WebType.getWebType(xmler.getAttributeValue(null, T_TYPE)), new URL(xmler.getAttributeValue(null, T_URL)));
        node.setTsuid(TransferSyntax.getTransferSyntax(xmler.getAttributeValue(null, T_TSUID)));
        return node;
    }

}