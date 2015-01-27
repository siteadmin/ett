package gov.nist.healthcare.ttt.xdr.other

import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import org.apache.commons.lang.StringEscapeUtils
import spock.lang.Specification

import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage

/**
 * Created by gerardin on 12/1/14.
 */
class UpdatedReportParsingTest extends Specification {


    def testReport() {

        given:
        //a hardcoded report
        def report = new XmlSlurper().parseText(report)

        when:
        //we parse it
        def tkValidationReport = new TkValidationReport()
        tkValidationReport.request = report.request.text()
        tkValidationReport.response = report.response.text()

        String request = report.request.header.text()
        request += report.request.body.text()

        String processed = StringEscapeUtils.unescapeXml(request)

        println processed


        InputStream is = new ByteArrayInputStream( request.getBytes() )


        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()),is)

        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        String xml = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");

        def envelope = new XmlSlurper().parseText(processed)

        def directFrom = envelope.Header.addressBlock.from.text()

        println directFrom



        then:
            println "ok"


    }


    def report =
            """
<transactionLog type='docrec' simId='1'>
    <request>
        <header>
            content-type: multipart/related; boundary="MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20"; type="application/xop+xml"; start="&lt;0.c41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70@apache.org&gt;"; start-info="application/soap+xml"; action="urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b"
            user-agent: TempXDRSender
            host: edge.nist.gov:8080

        </header>
        <body>

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20

            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;0.c41f86a92d39c3883023f2dbbaee45f5ae5bba5d4ffbfe70@apache.org&gt;


            &lt;s:Envelope xmlns:s=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:a=&quot;http://www.w3.org/2005/08/addressing&quot;&gt;
            &lt;soapenv:Header xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;direct:metadata-level xmlns:direct=&quot;urn:direct:addressing&quot;&gt;XDS&lt;/direct:metadata-level&gt;
            &lt;direct:addressBlock xmlns:direct=&quot;urn:direct:addressing&quot;
            soapenv:role=&quot;urn:direct:addressing:destination&quot;
            soapenv:relay=&quot;true&quot;&gt;
            &lt;direct:from&gt;directFrom&lt;/direct:from&gt;
            &lt;direct:to&gt;directTo&lt;/direct:to&gt;
            &lt;/direct:addressBlock&gt;
            &lt;wsa:To soapenv:mustUnderstand=&quot;true&quot; xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;http://transport-testing.nist.gov:12080/ttt/sim/f8488a75-fc7d-4d70-992b-e5b2c852b412/rep/prb&lt;/wsa:To&gt;
            &lt;wsa:MessageID soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;30f7b099-8886-48b7-8918-73c8e188dff2&lt;/wsa:MessageID&gt;
            &lt;wsa:Action soapenv:mustUnderstand=&quot;true&quot;
            xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;
            xmlns:wsa=&quot;http://www.w3.org/2005/08/addressing&quot;
            &gt;urn:ihe:iti:2007:ProvideAndRegisterDocumentSet-b&lt;/wsa:Action&gt;
            &lt;/soapenv:Header&gt;
            &lt;soapenv:Body xmlns:soapenv=&quot;http://www.w3.org/2003/05/soap-envelope&quot;&gt;
            &lt;xdsb:ProvideAndRegisterDocumentSetRequest xmlns:xdsb=&quot;urn:ihe:iti:xds-b:2007&quot;&gt;

            &lt;/xdsb:ProvideAndRegisterDocumentSetRequest&gt;
            &lt;/soapenv:Body&gt;
            &lt;/s:Envelope&gt;

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20
            Content-Type: application/xop+xml; charset=UTF-8; type="application/soap+xml"
            Content-Transfer-Encoding: binary
            Content-ID: &lt;0.dsadadasd3f2dbbaedasdasdasddsa@apache.org&gt;

            popp

            --MIMEBoundary_1293f28762856bdafcf446f2a6f4a61d95a95d0ad1177f20--
        </body>
    </request>
    <response>
    </response>
</transactionLog>
"""
}
