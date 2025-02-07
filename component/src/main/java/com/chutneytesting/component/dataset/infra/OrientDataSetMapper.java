package com.chutneytesting.component.dataset.infra;

import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CONSTANTS;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_CREATIONDATE;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DATATABLE;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_DESCRIPTION;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_NAME;
import static com.chutneytesting.component.scenario.infra.orient.OrientComponentDB.DATASET_CLASS_PROPERTY_TAGS;
import static java.time.temporal.ChronoUnit.MILLIS;

import com.chutneytesting.component.ComposableIdUtils;
import com.chutneytesting.component.dataset.domain.DataSet;
import com.orientechnologies.orient.core.metadata.schema.OType;
import com.orientechnologies.orient.core.record.OElement;
import java.sql.Date;

class OrientDataSetMapper {

    protected static void dataSetToElement(final DataSet dataSet, OElement oDataSet) {
        dataSetMetaDataToElement(dataSet, oDataSet);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_CONSTANTS, dataSet.constants, OType.EMBEDDEDMAP);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_DATATABLE, dataSet.datatable, OType.EMBEDDEDLIST);
    }

    private static void dataSetMetaDataToElement(final DataSet dataSet, OElement oDataSet) {
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_NAME, dataSet.name, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_DESCRIPTION, dataSet.description, OType.STRING);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_CREATIONDATE, Date.from(dataSet.creationDate), OType.DATETIME);
        oDataSet.setProperty(DATASET_CLASS_PROPERTY_TAGS, dataSet.tags, OType.EMBEDDEDLIST);
    }

    protected static DataSet elementToDataSet(OElement oDataSet) {
        DataSet.DataSetBuilder builder = elementToDataSetMetaDataBuilder(oDataSet);

        builder
            .withConstants(oDataSet.getProperty(DATASET_CLASS_PROPERTY_CONSTANTS))
            .withDatatable(oDataSet.getProperty(DATASET_CLASS_PROPERTY_DATATABLE));

        return builder.build();
    }

    protected static DataSet elementToDataSetMetaData(OElement oDataSet) {
        return elementToDataSetMetaDataBuilder(oDataSet).build();
    }

    protected static DataSet.DataSetBuilder elementToDataSetMetaDataBuilder(OElement oDataSet) {
        String internalId = oDataSet.getIdentity().toString();
        String externalId = ComposableIdUtils.toExternalId(internalId);
        return DataSet.builder()
            .withId(externalId)
            .withName(oDataSet.getProperty(DATASET_CLASS_PROPERTY_NAME))
            .withDescription(oDataSet.getProperty(DATASET_CLASS_PROPERTY_DESCRIPTION))
            .withCreationDate(((java.util.Date) oDataSet.getProperty(DATASET_CLASS_PROPERTY_CREATIONDATE)).toInstant().truncatedTo(MILLIS))
            .withTags(oDataSet.getProperty(DATASET_CLASS_PROPERTY_TAGS));
    }
}
