/**
 * Constellio, Open Source Enterprise Search
 * Copyright (C) 2010 DocuLibre inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package com.doculibre.constellio.utils;

import java.io.File;
import java.util.List;

import org.hibernate.cfg.AnnotationConfiguration;

import com.doculibre.constellio.entities.AdvancedSearchEnabledRule;
import com.doculibre.constellio.entities.Analyzer;
import com.doculibre.constellio.entities.AnalyzerClass;
import com.doculibre.constellio.entities.AnalyzerFilter;
import com.doculibre.constellio.entities.BaseConstellioEntity;
import com.doculibre.constellio.entities.Cache;
import com.doculibre.constellio.entities.Categorization;
import com.doculibre.constellio.entities.CategorizationRule;
import com.doculibre.constellio.entities.CollectionFacet;
import com.doculibre.constellio.entities.CollectionFederation;
import com.doculibre.constellio.entities.CollectionPermission;
import com.doculibre.constellio.entities.ConnectorInstance;
import com.doculibre.constellio.entities.ConnectorInstanceMeta;
import com.doculibre.constellio.entities.ConnectorManager;
import com.doculibre.constellio.entities.ConnectorType;
import com.doculibre.constellio.entities.ConnectorTypeMetaMapping;
import com.doculibre.constellio.entities.ConstellioEntity;
import com.doculibre.constellio.entities.ConstellioGroup;
import com.doculibre.constellio.entities.ConstellioLabelledEntity;
import com.doculibre.constellio.entities.ConstellioUser;
import com.doculibre.constellio.entities.CopyField;
import com.doculibre.constellio.entities.CredentialGroup;
import com.doculibre.constellio.entities.FeaturedLink;
import com.doculibre.constellio.entities.FederationRecordDeletionRequired;
import com.doculibre.constellio.entities.FederationRecordIndexingRequired;
import com.doculibre.constellio.entities.FieldType;
import com.doculibre.constellio.entities.FieldTypeClass;
import com.doculibre.constellio.entities.FilterClass;
import com.doculibre.constellio.entities.GroupParticipation;
import com.doculibre.constellio.entities.I18NLabel;
import com.doculibre.constellio.entities.IndexField;
import com.doculibre.constellio.entities.RecordCollection;
import com.doculibre.constellio.entities.SearchInterfaceConfig;
import com.doculibre.constellio.entities.SearchResultField;
import com.doculibre.constellio.entities.SolrConfig;
import com.doculibre.constellio.entities.SynonymList;
import com.doculibre.constellio.entities.TokenizerClass;
import com.doculibre.constellio.entities.UserCredentials;

public class ConstellioAnnotationUtils {

    public static void addAnnotatedClasses(AnnotationConfiguration config) {
        config.addAnnotatedClass(AdvancedSearchEnabledRule.class);
        config.addAnnotatedClass(Analyzer.class);
        config.addAnnotatedClass(AnalyzerClass.class);
        config.addAnnotatedClass(AnalyzerFilter.class);
        config.addAnnotatedClass(BaseConstellioEntity.class);
        config.addAnnotatedClass(Cache.class);
        config.addAnnotatedClass(Categorization.class);
        config.addAnnotatedClass(CategorizationRule.class);
        config.addAnnotatedClass(CollectionFacet.class);
        config.addAnnotatedClass(CollectionFederation.class);
        config.addAnnotatedClass(CollectionPermission.class);
        config.addAnnotatedClass(ConnectorInstance.class);
        config.addAnnotatedClass(ConnectorInstanceMeta.class);
        config.addAnnotatedClass(ConnectorManager.class);
        config.addAnnotatedClass(ConnectorType.class);
        config.addAnnotatedClass(ConnectorTypeMetaMapping.class);
        config.addAnnotatedClass(ConstellioEntity.class);
        config.addAnnotatedClass(ConstellioGroup.class);
        config.addAnnotatedClass(ConstellioLabelledEntity.class);
        config.addAnnotatedClass(ConstellioUser.class);
        config.addAnnotatedClass(CopyField.class);
        config.addAnnotatedClass(CredentialGroup.class);
        config.addAnnotatedClass(FeaturedLink.class);
        config.addAnnotatedClass(FederationRecordDeletionRequired.class);
        config.addAnnotatedClass(FederationRecordIndexingRequired.class);
        config.addAnnotatedClass(FieldType.class);
        config.addAnnotatedClass(FieldTypeClass.class);
        config.addAnnotatedClass(FilterClass.class);
        config.addAnnotatedClass(GroupParticipation.class);
        config.addAnnotatedClass(I18NLabel.class);
        config.addAnnotatedClass(IndexField.class);
        config.addAnnotatedClass(RecordCollection.class);
        config.addAnnotatedClass(SearchInterfaceConfig.class);
        config.addAnnotatedClass(SearchResultField.class);
        config.addAnnotatedClass(SolrConfig.class);
        config.addAnnotatedClass(SynonymList.class);
        config.addAnnotatedClass(TokenizerClass.class);
        config.addAnnotatedClass(UserCredentials.class);
    }

    public static void main(String[] args) {
        // Will write the code that needs to be placed in addAnnotatedClasses(AnnotationConfiguration)
        // method...
        File srcDir = new File(args[0]);
        String entitiesDirPath = "com.doculibre.constellio.entities".replace(".", File.separator);
        File entitiesDir = new File(srcDir, entitiesDirPath);
        List<File> javaFiles = (List<File>) org.apache.commons.io.FileUtils.listFiles(entitiesDir,
            new String[] { ".java" }, true);
        for (File javaFile : javaFiles) {
            System.out.println("config.addAnnotatedClass(" + javaFile.getName().replace("java", ".class")
                + ");");
        }
    }

}
