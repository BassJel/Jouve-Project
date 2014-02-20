package com.doculibre.constellio.solr.handler.component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.solr.cloud.ZkController;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.update.processor.UpdateRequestProcessorFactory;
import org.apache.solr.util.DOMUtil;
import org.apache.solr.util.VersionedFile;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.apache.solr.core.Config;
import org.apache.solr.core.SolrCore;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class CategoryUpdateProcessorFactory extends UpdateRequestProcessorFactory implements SolrCoreAware {
//	private static Logger log = LoggerFactory.getLogger(CategoryUpdateProcessorFactory.class);

	private String configFile;
	private Set<Categorization> categorizations;

	@Override
	public void init(final NamedList args) {
		if (args != null) {
			SolrParams params = SolrParams.toSolrParams(args);
			this.configFile = params.get("config-file", "category.xml");
		}
	}

	@Override
	public void inform(SolrCore core) {
		try {
			boolean exists = true;
			Config cfg;
			// check if using ZooKeeper
			ZkController zkController = core.getCoreDescriptor().getCoreContainer().getZkController();
			if (zkController != null) {
				exists = zkController.configFileExists(zkController.readConfigName(core.getCoreDescriptor().getCloudDescriptor().getCollectionName()), configFile);
				if (exists) {
					cfg = new Config(core.getResourceLoader(), configFile);
				}
				else{
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "CategoryUpdateProcessorFactory missing config file: '" + configFile + " in zookeeper\n");
				}
			} else {
				File fC = new File(core.getResourceLoader().getConfigDir(), configFile);
				File fD = new File(core.getDataDir(), configFile);
				if (fC.exists() == fD.exists()) {
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "CategoryUpdateProcessorFactory missing config file: '" + configFile + "\n" + "either: " + fC.getAbsolutePath()
							+ " or " + fD.getAbsolutePath() + " must exist, but not both.");
				}
				if (fC.exists()) {
					//log.info("Loading Category Configuration from: " + fC.getAbsolutePath());
					cfg = new Config(core.getResourceLoader(), configFile);
				} else {
					InputStream is = VersionedFile.getLatestFile(core.getDataDir(), configFile);
					cfg = new Config(core.getResourceLoader(), configFile, new InputSource(is), null);
				}
				
			}
			loadCategorization(cfg);
		} catch (Exception ex) {
			throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "Error initializing CategoryUpdateProcessorFactory.", ex);
		}

	}

	@Override
	public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
		return new CategoryUpdateProcessor(next);
	}

	private class CategoryUpdateProcessor extends UpdateRequestProcessor {

		public CategoryUpdateProcessor(UpdateRequestProcessor next) {
			super(next);
		}

		@Override
		public void processAdd(AddUpdateCommand cmd) throws IOException {
			SolrInputDocument doc = cmd.getSolrInputDocument();

			for (Categorization categorization : categorizations) {
				String destField = categorization.getDestField();
				if (destField != null && !destField.isEmpty()) {

					for (CategorizationRule categorizationRule : categorization.getCategorizationRules()) {
						String srcField = categorizationRule.getSrcField();
						boolean isMatched = false;
						if (srcField != null && !srcField.isEmpty()) {
							Collection<Object> srcFieldValues = doc.getFieldValues(srcField);
							if (srcFieldValues != null && !srcFieldValues.isEmpty()) {
								Pattern pattern = Pattern.compile(categorizationRule.getMatchRegexp(), Pattern.CASE_INSENSITIVE);
								for (Object srcFieldValue : srcFieldValues) {
									Matcher matcher = pattern.matcher(srcFieldValue.toString());
									if (matcher.find()) {
										isMatched = true;
										break;
									}
								}
								if (isMatched) {
									for (String Value:categorizationRule.getMatchRegexpIndexedValues()) {
										doc.addField(destField,Value);
									}
								}
							}
						}
					}
				}
			}
			cmd.solrDoc = doc;
			super.processAdd(cmd);
		}
	}

	// load up the category configuration
	private void loadCategorization(Config config){
		XPath xpath = XPathFactory.newInstance().newXPath();
		Set<Categorization> categorizations = new HashSet<Categorization>();
		NodeList nodes = (NodeList) config.evaluate("categories/category", XPathConstants.NODESET);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node node = nodes.item(i);
			Categorization categorization = new Categorization();
			String name = DOMUtil.getAttr(node, "name", "missing category 'name'");
			categorization.setName(name);
			String destField = DOMUtil.getAttr(node, "destField", "missing category 'destField'");
			categorization.setDestField(destField);

			NodeList children = null;
			try {
				children = (NodeList) xpath.evaluate("rule", node, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "category requires '<rule .../>' child");
			}

			for (int j = 0; j < children.getLength(); j++) {
				Node child = children.item(j);
				CategorizationRule categorizationRule = new CategorizationRule();
				String srcField = DOMUtil.getAttr(child, "srcField", "missing 'srcField'");
				categorizationRule.setSrcField(srcField);
				String matchRegexp = DOMUtil.getAttr(child, "regex", "missing 'regex'");
				categorizationRule.setMatchRegexp(matchRegexp);

				NodeList grandChildren = null;
				try {
					grandChildren = (NodeList) xpath.evaluate("value", child, XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, "rule requires '<value .../>' child");
				}

				for (int k = 0; k < grandChildren.getLength(); k++) {
					Node grandChild = grandChildren.item(k);
					String value = DOMUtil.getAttr(grandChild, "value", "missing 'value'");
					categorizationRule.addMatchRegexpIndexedValue(value);
				}

				categorization.addCategorizationRule(categorizationRule);
			}

			categorizations.add(categorization);
		}
		this.categorizations = categorizations;
	}
}