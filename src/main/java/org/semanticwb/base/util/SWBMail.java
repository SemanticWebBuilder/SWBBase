/*
 * SemanticWebBuilder es una plataforma para el desarrollo de portales y aplicaciones de integración,
 * colaboración y conocimiento, que gracias al uso de tecnología semántica puede generar contextos de
 * información alrededor de algún tema de interés o bien integrar información y aplicaciones de diferentes
 * fuentes, donde a la información se le asigna un significado, de forma que pueda ser interpretada y
 * procesada por personas y/o sistemas, es una creación original del Fondo de Información y Documentación
 * para la Industria INFOTEC, cuyo registro se encuentra actualmente en trámite.
 *
 * INFOTEC pone a su disposición la herramienta SemanticWebBuilder a través de su licenciamiento abierto al público ('open source'),
 * en virtud del cual, usted podrá usarlo en las mismas condiciones con que INFOTEC lo ha diseñado y puesto a su disposición;
 * aprender de él; distribuirlo a terceros; acceder a su código fuente y modificarlo, y combinarlo o enlazarlo con otro software,
 * todo ello de conformidad con los términos y condiciones de la LICENCIA ABIERTA AL PÚBLICO que otorga INFOTEC para la utilización
 * del SemanticWebBuilder 4.0.
 *
 * INFOTEC no otorga garantía sobre SemanticWebBuilder, de ninguna especie y naturaleza, ni implícita ni explícita,
 * siendo usted completamente responsable de la utilización que le dé y asumiendo la totalidad de los riesgos que puedan derivar
 * de la misma.
 *
 * Si usted tiene cualquier duda o comentario sobre SemanticWebBuilder, INFOTEC pone a su disposición la siguiente
 * dirección electrónica:
 *  http://www.semanticwebbuilder.org.mx
 */
package org.semanticwb.base.util;

import org.apache.commons.mail.EmailAttachment;

import javax.mail.internet.InternetAddress;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Wrapper class to hold e-mail message information.
 * @author jorge.jimenez
 */
public class SWBMail {
    private String senderEmail = null;
    private String senderName = null;
    private Collection<InternetAddress> toEmail = null;
    private Collection<InternetAddress> ccEmail = null;
    private Collection<InternetAddress> bccEmail = null;
    private List<EmailAttachment> attachments = new ArrayList<>();
    private List<InternetAddress> addresses = new ArrayList<>();
    private String login;
    private String password;
    private String subject = null;
    private String message = null;
    private String contentType = null;
    private String smtpserver = null;

    /**
     * Constructor. Creates a new instance of {@link SWBMail}.
     */
    public SWBMail() { }

    /**
     * Constructor. Creates a new instance of {@link SWBMail}.
     * 
     * @param recipients List of {@link InternetAddress} objects holding destination addresses.
     * @param subject E-mail subject.
     * @param body E-mail body.
     */
    public SWBMail(@NotNull Collection<InternetAddress> recipients, @NotNull String subject, @NotNull String body) {
        this.senderEmail = "webmail.infotec.com.mx";
        this.toEmail = recipients;
        this.subject = subject;
        this.message = body;
    }

    /**
     * Constructor. Creates a new instance of {@link SWBMail}.
     * 
     * @param sender Sender e-mail address.
     * @param recipients List of {@link InternetAddress} objects holding destination addresses.
     * @param ccRecipients List of {@link InternetAddress} objects holding copy addresses.
     * @param bccRecipients List of {@link InternetAddress} objects holding carbon copy addresses.
     * @param subject E-mail subject.
     * @param body E-mail boody.
     */
    public SWBMail(@NotNull String sender, @NotNull Collection<InternetAddress> recipients,
                   Collection<InternetAddress> ccRecipients,
                   Collection<InternetAddress> bccRecipients, @NotNull String subject, @NotNull String body) {
        this.senderEmail = sender;
        this.toEmail = recipients;
        this.ccEmail = ccRecipients;
        this.bccEmail = bccRecipients;
        this.subject = subject;
        this.message = body;
    }

    /**
     * @deprecated Use {@link #setAddresses(List)} method instead.
     * Sets addresses.
     * @param addresses the new addresses list.
     */
    @Deprecated
    public void setAddress(ArrayList<InternetAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     * Sets recipient addresses for the e-mail.
     * @param addresses List of {@link InternetAddress} objects.
     */
    public void setAddresses(@NotNull List<InternetAddress> addresses) {
        this.addresses = addresses;
    }

    /**
     * Gets the addresses list.
     * @return Value of property addresses.
     */
    public List<InternetAddress> getAddresses() {
        return addresses;
    }

    /**
     * Adds an address to the list as an {@link InternetAddress} object.
     * @param address the address.
     */
    public void addAddress(@NotNull InternetAddress address) {
        addresses.add(address);
    }

    /**
     * Adds an address to the list as a String.
     * @param address the address
     */
    public void addAddress(String address) {
        if (null != address && !address.isEmpty()) {
            InternetAddress inetAddress = new InternetAddress();
            inetAddress.setAddress(address);
            addresses.add(inetAddress);
        }
    }

    /**
     * @deprecated Use {@link #setAddresses(List)} instead.
     * Sets e-mail attachments.
     * @param attachments the new attachments
     */
    @Deprecated
    public void setAttachments(ArrayList<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    /**
     * Sets e-mail attachments.
     * @param attachments List of {@link EmailAttachment} objects.
     */
    public void setAttachments(@NotNull List<EmailAttachment> attachments) {
        this.attachments = attachments;
    }

    /** Gets the list of attachments.
     * @return List of attachments.
     */
    public List<EmailAttachment> getAttachments() {
        return attachments;
    }

    /**
     * Adds an attachment.
     * @param attachment the attachment
     */
    public void addAttachment(@NotNull EmailAttachment attachment) {
        attachments.add(attachment);
    }

    /**
     * @deprecated Use getSenderEmail instead for naming consistency.
     * Gets e-mail sender.
     * @return E-mail address of sender.
     */
    @Deprecated
    public String getFromEmail() {
        return senderEmail;
    }

    /**
     * Gets the sender e-mail.
     * @return E-mail address of sender.
     */
    public String getSenderEmail() {
        return senderEmail;
    }

    /**
     * @deprecated use {@link #setSenderEmail(String)} method instead.
     * Sets the sender e-mail.
     * @param senderMail sender e-mail.
     */
    @Deprecated
    public void setFromEmail(String senderMail) {
        this.senderEmail = senderMail;
    }

    /**
     * Sets the sender e-mail.
     * @param senderMail Sender e-mail.
     */
    public void setSenderEmail(@NotNull String senderMail) {
        this.senderEmail = senderMail;
    }
    
    /**
     * @deprecated Use {@link #getSenderName()} method instead.
     * Gets sender name.
     * @return Sender name.
     */
    @Deprecated
    public String getFromName() {
        return senderName;
    }

    /**
     * Gets the sender name.
     * @return Sender name.
     */
    public String getSenderName() {
        return senderName;
    }

    /**
     * @deprecated Use {@link #setSenderName(String)} method instead.
     * Sets the sender name.
     * @param senderName sender name.
     */
    @Deprecated
    public void setFromName(String senderName) {
        this.senderName = senderName;
    }

    /**
     * Sets the sender name.
     * @param senderName Sender name.
     */
    public void setSenderName(@NotNull String senderName) {
        this.senderName = senderName;
    }
    

    /**
     * @deprecated Use {@link #getRecipients()} method instead.
     * Gets recipient e-mail list.
     * @return List of {@link InternetAddress} objects of recipients.
     */
    @Deprecated
    public Collection<InternetAddress> getToEmail() {
        return toEmail;
    }

    /**
     * Gets recipients list.
     * @return Recipients list.
     */
    public Collection<InternetAddress> getRecipients() {
        return toEmail;
    }

    /**
     * @deprecated Use {@link #setRecipients(Collection)} method instead.
     * Sets recipient e-mail list
     * @param recipients List of {@link InternetAddress} objects of recipients.
     */
    @Deprecated
    public void setToEmail(Collection<InternetAddress> recipients) {
        this.toEmail = recipients;
    }

    /**
     * Sets recipient e-mail list
     * @param recipients List of {@link InternetAddress} objects of recipients.
     */
    public void setRecipients(@NotNull Collection<InternetAddress> recipients) {
        this.toEmail = recipients;
    }

    /**
     * @deprecated Use {@link #getCcRecipients()} method instead.
     * Gets list of copy recipients.
     * @return List of copy recipients..
     */
    @Deprecated
    public Collection<InternetAddress> getCcEmail() {
        return ccEmail;
    }

    /**
     * Gets list of copy recipients.
     * @return List of copy recipients..
     */
    public Collection<InternetAddress> getCcRecipients() {
        return ccEmail;
    }

    /**
     * @deprecated Use {@link #setCcRecipients(Collection)}
     * Sets list of copy recipients.
     * @param copyRecipients List of {@link InternetAddress} objects of copy recipients.
     */
    @Deprecated
    public void setCcEmail(Collection<InternetAddress> copyRecipients) {
        this.ccEmail = copyRecipients;
    }

    /**
     * Sets list of copy recipients.
     * @param copyRecipients List of {@link InternetAddress} objects of copy recipients.
     */
    public void setCcRecipients(@NotNull Collection<InternetAddress> copyRecipients) {
        this.ccEmail = copyRecipients;
    }

    /**
     * @deprecated Use {@link #getBccRecipients()} method instead.
     * Gets list of copy recipients.
     * @return List of copy recipients.
     */
    @Deprecated
    public Collection<InternetAddress> getBccEmail() {
        return bccEmail;
    }

    /**
     * Gets list of copy recipients.
     * @return List of copy recipients.
     */
    public Collection<InternetAddress> getBccRecipients() {
        return bccEmail;
    }

    /**
     * @deprecated Use {@link #setBccRecipients(Collection)} method instead.
     * Sets list of carbon copy recipients.
     * @param carbonCopyRecipients New value of property bccEmail.
     */
    @Deprecated
    public void setBccEmail(Collection<InternetAddress> carbonCopyRecipients) {
        this.bccEmail = carbonCopyRecipients;
    }

    /**
     * Sets list of carbon copy recipients.
     * @param carbonCopyRecipients New value of property bccEmail.
     */
    public void setBccRecipients(@NotNull Collection<InternetAddress> carbonCopyRecipients) {
        this.bccEmail = carbonCopyRecipients;
    }

    /**
     * @deprecated Use {@link #getMessage()} method instead.
     * Gets e-mail body.
     * @return e-mail body
     */
    @Deprecated
    public String getData() {
        return message;
    }

    /***
     * Gets e-mail body.
     * @return e-mail body
     */
    public String getMessage() {
        return message;
    }

    /**
     * @deprecated Use {@link #setMessage(String)} method instead.
     * Setter for property data.
     * @param data New value of property data.
     */
    @Deprecated
    public void setData(String data) {
        this.message = data;
    }

    /**
     * Sets e-mail boy.
     * @param message Message String
     */
    public void setMessage(@NotNull String message) {
        this.message = message;
    }

    /** Getter for property login.
     * @return Value of property login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * Setter for property login.
     * @param login the new login
     */
    public void setLogin(@NotNull String login) {
        this.login = login;
    }

    /** Getter for property password.
     * @return Value of property password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter for property password.
     * @param password the new password
     */
    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    /** Getter for property contentType.
     * @return Value of property contentType.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Setter for property contentType.
     * @param contentType the new content type
     */
    public void setContentType(@NotNull String contentType) {
        this.contentType = contentType;
    }

    /** Getter for property subject.
     * @return Value of property subject.
     */
    public String getSubject() {
        return subject;
    }

    /** Setter for property subject.
     * @param subject New value of property subject.
     */
    public void setSubject(@NotNull String subject) {
        this.subject = subject;
    }
    
    /**
     * @deprecated Not intended to be used anymore.
     * Setter for property smtpserver.
     * @param smtpserver the new host name
     */
    @Deprecated
    public void setHostName(String smtpserver) {
        this.smtpserver = smtpserver;
    }
    
    /**
     * @deprecated Not intended to be used anymore.
     * Getter for property smtpserver.
     * @return the host name
     */
    @Deprecated
    public String getHostName() {
        return smtpserver;
    }
}
