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

import org.semanticwb.Logger;
import org.semanticwb.SWBUtils;

import java.util.LinkedList;

/**
 * Tread responsible for sending queued e-mails.
 * @author Javier Solís
 */
public class SWBMailSender extends java.lang.Thread {
	private static Logger LOG = SWBUtils.getLogger(SWBMailSender.class);
	LinkedList<SWBMail> emails;

	/**
	 * Constructor. Creates a new instance of {@link SWBMailSender}.
	 */
	public SWBMailSender() {
		emails = new LinkedList<>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while (!emails.isEmpty()) {
				SWBMail email = emails.removeLast();
				SWBUtils.EMAIL.sendMail(email.getSenderEmail(), email.getSenderName(), email.getAddresses(),
						email.getCcRecipients(), email.getBccRecipients(), email.getSubject(), email.getContentType(),
						email.getMessage(), email.getLogin(), email.getPassword(), email.getAttachments());
			}
		} catch (Exception e) {
			LOG.error(e);
		}
	}

	/**
	 * Adds a {@link SWBMail} to the queue.
	 * @param email {@link SWBMail}.
	 */
	public void addEMail(SWBMail email) {
		emails.addFirst(email);
	}
}
