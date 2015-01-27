package gov.nist.healthcare.ttt.xdr.web
import gov.nist.healthcare.ttt.commons.notification.Message
import gov.nist.healthcare.ttt.database.xdr.XDRRecordInterface
import gov.nist.healthcare.ttt.xdr.api.XdrReceiver
import gov.nist.healthcare.ttt.xdr.domain.TkValidationReport
import groovy.util.slurpersupport.GPathResult
import org.apache.commons.lang.StringEscapeUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController

import javax.mail.Multipart
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
/**
 * Created by gerardin on 10/14/14.
 *
 * Listener for for the toolkit.
 * It listens for new validation reports.
 * It tries to handle properly communication errors (if it does not understand the payload)
 */


@RestController
public class TkListener {

    private static Logger log = LoggerFactory.getLogger(TkListener)

    //TODO @Antoine. should be used to configure the endpoint or at least to check config
    @Value('${xdr.notification}')
    private String notificationUrl


    @Autowired
    XdrReceiver receiver

    /**
     * Notify of a new validation report
     * @param body : the report
     */
    @RequestMapping(value = 'api/xdrNotification', consumes = "application/xml")
    @ResponseBody
    public void receiveBySimulatorId(@RequestBody String body) {

        log.debug("receive a new validation report: $body")
        Message m = null

        try {

            def report = new XmlSlurper().parseText(body)

            def tkValidationReport = new TkValidationReport()
            tkValidationReport.request = report.request.text()
            tkValidationReport.response = report.response.text()
            tkValidationReport.simId = report.@simId.text()

            //Extract direct from address
            tkValidationReport.directFrom = parseRequest(report)

                    //TODO modify : all that to extract registryResponseStatus info!
            String content = report.response.body.text()
            def registryResponse = content.split("<.?S:Body>")
            def registryResponseXml = new XmlSlurper().parseText(registryResponse[1])
            def registryResponseStatus = registryResponseXml.@status.text()
            def criteriaMet = parseStatus(registryResponseStatus)
            tkValidationReport.status = criteriaMet

            m = new Message<TkValidationReport>(Message.Status.SUCCESS, "new validation result received...", tkValidationReport)
        }
        catch(Exception e) {
            log.error("receive an invalid validation report. Bad payload rejected :\n $body")

            m = new Message<TkValidationReport>(Message.Status.ERROR, "new validation result received...")
        }
        finally {
            receiver.notifyObserver(m)
        }
    }

    def parseStatus(String registryResponseStatus) {
        if (registryResponseStatus.contains("Failure")) {
            return XDRRecordInterface.CriteriaMet.FAILED
        } else if (registryResponseStatus.contains("Success")) {
            return XDRRecordInterface.CriteriaMet.PASSED
        }
    }

    def parseRequest(GPathResult report){

        String request = report.request.body.text()

        InputStream is = new ByteArrayInputStream( request.getBytes() )


        MimeMessage msg = new MimeMessage(Session.getDefaultInstance(new Properties()),is)

        Multipart content = msg.getContent()
        MimeBodyPart part1 = content.getBodyPart(0)

        ByteArrayOutputStream out = new ByteArrayOutputStream()
        part1.writeTo(out)
        println out.toString()

        String xml = org.apache.commons.io.IOUtils.toString(part1.getInputStream(), "UTF-8");

        String processed = StringEscapeUtils.unescapeXml(xml)

        println processed

        def envelope = new XmlSlurper().parseText(processed)

        def directFrom = envelope.Header.addressBlock.from.text()

        println directFrom
    }
}
