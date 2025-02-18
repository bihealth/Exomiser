/*
 * The Exomiser - A tool to annotate and prioritize genomic variants
 *
 * Copyright (c) 2016-2019 Queen Mary University of London.
 * Copyright (c) 2012-2016 Charité Universitätsmedizin Berlin and Genome Research Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.monarchinitiative.exomiser.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.charite.compbio.jannovar.annotation.VariantEffect;
import de.charite.compbio.jannovar.mendel.ModeOfInheritance;
import de.charite.compbio.jannovar.pedigree.Genotype;
import htsjdk.variant.variantcontext.GenotypesContext;
import htsjdk.variant.variantcontext.VariantContext;
import htsjdk.variant.variantcontext.VariantContextBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.monarchinitiative.exomiser.core.filters.FilterResult;
import org.monarchinitiative.exomiser.core.filters.FilterType;
import org.monarchinitiative.exomiser.core.genome.GenomeAssembly;
import org.monarchinitiative.exomiser.core.model.frequency.Frequency;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencyData;
import org.monarchinitiative.exomiser.core.model.frequency.FrequencySource;
import org.monarchinitiative.exomiser.core.model.frequency.RsId;
import org.monarchinitiative.exomiser.core.model.pathogenicity.*;

import java.util.*;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for non-bean (i.e. logic-containing) methods in
 * {@code VariantEvaluation} class
 *
 * @author Jules Jacobsen <jules.jacobsen@sanger.ac.uk>
 */
public class VariantEvaluationTest {

    private VariantEvaluation instance;

    private static final GenomeAssembly GENOME_ASSEMBLY = GenomeAssembly.HG19;
    private static final int CHROMOSOME = 1;
    private static final String CHROMOSOME_NAME = "1";
    private static final int POSITION = 1;
    private static final String REF = "C";
    private static final String ALT = "T";

    private static final double QUALITY = 2.2;
    private static final int READ_DEPTH = 6;
    private static final Genotype HETEROZYGOUS = Genotype.HETEROZYGOUS;
    private static final String GENE1_GENE_SYMBOL = "GENE1";
    private static final String GENE1_GENE_ID = "1234567";

    private static final String GENE2_GENE_SYMBOL = "GENE2";
    private static final String GENE2_GENE_ID = "7654321";

    private static final FilterResult FAIL_FREQUENCY_RESULT = FilterResult.fail(FilterType.FREQUENCY_FILTER);
    private static final FilterResult PASS_FREQUENCY_RESULT = FilterResult.pass(FilterType.FREQUENCY_FILTER);

    private static final FilterResult PASS_QUALITY_RESULT = FilterResult.pass(FilterType.QUALITY_FILTER);


    private static final float SIFT_PASS_SCORE = SiftScore.SIFT_THRESHOLD - 0.01f;
    private static final float SIFT_FAIL_SCORE = SiftScore.SIFT_THRESHOLD + 0.01f;

    private static final SiftScore SIFT_PASS = SiftScore.of(SIFT_PASS_SCORE);
    private static final SiftScore SIFT_FAIL = SiftScore.of(SIFT_FAIL_SCORE);

    private static final float POLYPHEN_PASS_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD + 0.1f;
    private static final float POLYPHEN_FAIL_SCORE = PolyPhenScore.POLYPHEN_THRESHOLD - 0.1f;

    private static final PolyPhenScore POLYPHEN_PASS = PolyPhenScore.of(POLYPHEN_PASS_SCORE);
    private static final PolyPhenScore POLYPHEN_FAIL = PolyPhenScore.of(POLYPHEN_FAIL_SCORE);

    private static final float MTASTER_PASS_SCORE = MutationTasterScore.MTASTER_THRESHOLD + 0.01f;
    private static final float MTASTER_FAIL_SCORE = MutationTasterScore.MTASTER_THRESHOLD - 0.01f;

    private static final MutationTasterScore MTASTER_PASS = MutationTasterScore.of(MTASTER_PASS_SCORE);
    private static final MutationTasterScore MTASTER_FAIL = MutationTasterScore.of(MTASTER_FAIL_SCORE);

    @BeforeEach
    public void setUp() {
        instance = newInstance();
    }

    private VariantEvaluation newInstance() {
        return VariantEvaluation.builder(CHROMOSOME, POSITION, REF, ALT)
                .quality(QUALITY)
                .geneSymbol(GENE1_GENE_SYMBOL)
                .geneId(GENE1_GENE_ID)
                .build();
    }

    private static VariantEvaluation.Builder testVariantBuilder() {
        return VariantEvaluation.builder(CHROMOSOME, POSITION, REF, ALT);
    }

    @Test
    public void testDefaultGenomeAssembly() {
        assertThat(instance.getGenomeAssembly(), equalTo(GENOME_ASSEMBLY));
    }

    @Test
    public void testSpecifiedGenomeAssembly() {
        VariantEvaluation variantEvaluation = VariantEvaluation.builder(CHROMOSOME, POSITION, REF, ALT)
                .genomeAssembly(GenomeAssembly.HG38)
                .build();
        assertThat(variantEvaluation.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void testSpecifiedGenomeAssemblyFromValue() {
        VariantEvaluation variantEvaluation = VariantEvaluation.builder(CHROMOSOME, POSITION, REF, ALT)
                .genomeAssembly("GRCh38")
                .build();
        assertThat(variantEvaluation.getGenomeAssembly(), equalTo(GenomeAssembly.HG38));
    }

    @Test
    public void testSpecifiedGenomeAssemblyUnrecognisedAssemblyThrowsException() {
        assertThrows(RuntimeException.class, () -> VariantEvaluation.builder(CHROMOSOME, POSITION, REF, ALT)
                .genomeAssembly("wibble")
        );
    }

    @Test
    public void testGetChromosome() {
        assertThat(instance.getChromosome(), equalTo(CHROMOSOME));
    }

    @Test
    public void testGetChromosomeName() {
        assertThat(instance.getChromosomeName(), equalTo(CHROMOSOME_NAME));
    }

    @Test
    public void testBuilderChromosomeName() {
        VariantEvaluation variantEvaluation = testVariantBuilder().chromosomeName("Can be anything").build();
        assertThat(variantEvaluation.getChromosomeName(), equalTo("Can be anything"));
    }

    @Test
    public void testGetChromosomePosition() {
        assertThat(instance.getPosition(), equalTo(POSITION));
    }

    @Test
    public void testGetRef() {
        assertThat(instance.getRef(), equalTo(REF));
    }

    @Test
    public void testGetAlt() {
        assertThat(instance.getAlt(), equalTo(ALT));

    }

    @Test
    void testGeneSymbolCannotBeNull() {
        assertThrows(NullPointerException.class, () ->
                testVariantBuilder()
                .geneSymbol(null)
                        .build()
        );

    }

    @Test
    void testGeneSymbolCannotBeEmpty() {
        Throwable throwable = assertThrows(IllegalArgumentException.class, () ->
                testVariantBuilder()
                        .geneSymbol("")
                        .build()
        );
        assertThat(throwable.getMessage(), containsString("Variant gene symbol cannot be empty"));
    }

    @Test
    public void testGetGeneSymbol() {
        assertThat(instance.getGeneSymbol(), equalTo(GENE1_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsOnlyFirstGeneSymbol() {
        instance = testVariantBuilder()
                .geneSymbol(GENE2_GENE_SYMBOL + "," + GENE1_GENE_SYMBOL)
                .build();
        assertThat(instance.getGeneSymbol(), equalTo(GENE2_GENE_SYMBOL));
    }

    @Test
    public void testGetGeneSymbolReturnsADotIfGeneSymbolNotDefined() {
        instance = testVariantBuilder().build();
        assertThat(instance.getGeneSymbol(), equalTo("."));
    }

    @Test
    public void canGetGeneId() {
        assertThat(instance.getGeneId(), equalTo(GENE1_GENE_ID));
    }

    @Test
    void testSampleGenotypesCannotBeNull() {
        assertThrows(NullPointerException.class, () -> testVariantBuilder()
                .sampleGenotypes(null)
                .build()
        );
    }

    @Test
    void testSampleGenotypeEmptyIsReplacedWithDefault() {
        VariantEvaluation variantEvaluation = testVariantBuilder()
                .sampleGenotypes(Collections.emptyMap())
                .build();

        assertThat(variantEvaluation.getSampleGenotypes(), equalTo(VariantEvaluation.Builder.SINGLE_SAMPLE_HET_GENOTYPE));
    }

    @Test
    public void testSampleGenotypes() {
        Map<String, SampleGenotype> sampleGenotypes = ImmutableMap.of("Zaphod", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT));
        instance = testVariantBuilder()
                .sampleGenotypes(sampleGenotypes)
                .build();
        assertThat(instance.getSampleGenotypes(), equalTo(sampleGenotypes));
    }

    @Test
    public void testGetSampleGenotype() {
        SampleGenotype sampleGenotype = SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT);
        Map<String, SampleGenotype> sampleGenotypes = ImmutableMap.of("Zaphod", sampleGenotype);
        instance = testVariantBuilder()
                .sampleGenotypes(sampleGenotypes)
                .build();
        assertThat(instance.getSampleGenotype("Zaphod"), equalTo(sampleGenotype));
        assertThat(instance.getSampleGenotype("Nemo"), equalTo(SampleGenotype.empty()));
    }

    @Test
    public void testGetSampleGenotypesAreOrdered() {
        Map<String, SampleGenotype> sampleGenotypes = ImmutableMap.of(
                "Zaphod", SampleGenotype.of(AlleleCall.REF, AlleleCall.ALT),
                "Arthur", SampleGenotype.homRef(),
                "Trillian", SampleGenotype.homAlt(),
                "Marvin", SampleGenotype.noCall(),
                // ALT/OTHER_ALT is a 1/2 genotype
                "Ford", SampleGenotype.of(AlleleCall.ALT, AlleleCall.OTHER_ALT)
        );

        instance = testVariantBuilder()
                .sampleGenotypes(sampleGenotypes)
                .build();

        List<String> sampleNames = ImmutableList.copyOf(instance.getSampleGenotypes().keySet());
        assertThat(sampleNames, equalTo(ImmutableList.of("Zaphod", "Arthur", "Trillian", "Marvin", "Ford")));
        assertThat(instance.getGenotypeString(), equalTo("0/1:0/0:1/1:./.:-/1"));
    }

    @Test
    public void testGetGenotypeNoSampleIsHet() {
        instance = VariantEvaluation.builder(25, 1, "A", "T").build();
        assertThat(instance.getGenotypeString(), equalTo("0/1"));
    }

    @Test
    public void testCanSetVariantEffectAfterConstruction() {
        VariantEvaluation variantEvaluation = testVariantBuilder().variantEffect(VariantEffect.FEATURE_TRUNCATION)
                .build();
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.FEATURE_TRUNCATION));

        variantEvaluation.setVariantEffect(VariantEffect.MISSENSE_VARIANT);
        assertThat(variantEvaluation.getVariantEffect(), equalTo(VariantEffect.MISSENSE_VARIANT));
    }

    @Test
    void testsTranscriptAnnotationsCannotBeNull() {
        assertThrows(NullPointerException.class, () -> testVariantBuilder()
                .annotations(null)
                .build()
        );
    }

    @Test
    public void testTranscriptAnnotationsAreEmptyByDefault() {
        VariantEvaluation variantEvaluation = testVariantBuilder().build();
        assertThat(variantEvaluation.getTranscriptAnnotations(), equalTo(Collections.emptyList()));
    }

    @Test
    public void testAddTranscriptAnnotations() {
        TranscriptAnnotation transcriptAnnotation = TranscriptAnnotation.builder().geneSymbol("GENE1").build();
        List<TranscriptAnnotation> annotations = Collections.singletonList(transcriptAnnotation);
        VariantEvaluation variantEvaluation = testVariantBuilder()
                .annotations(annotations)
                .build();
        System.out.println(annotations);
        assertThat(variantEvaluation.getTranscriptAnnotations(), equalTo(annotations));
    }

    @Test
    public void testThatTheConstructorCreatesAnEmptyFrequencyDataObject() {
        FrequencyData frequencyData = instance.getFrequencyData();
        assertThat(frequencyData, equalTo(FrequencyData.empty()));
    }
    
    @Test
    public void testThatTheBuilderCanSetAFrequencyDataObject() {
        FrequencyData frequencyData = FrequencyData.of(RsId.of(12345), Frequency.of(FrequencySource.LOCAL, 0.1f));
        instance = testVariantBuilder().frequencyData(frequencyData).build();
        assertThat(instance.getFrequencyData(), equalTo(frequencyData));
    }

    @Test
    public void testCanSetFrequencyDataAfterConstruction() {
        FrequencyData frequencyData = FrequencyData.of(RsId.of(12345), Frequency.of(FrequencySource.LOCAL, 0.1f));
        instance.setFrequencyData(frequencyData);
        assertThat(instance.getFrequencyData(), equalTo(frequencyData));
    }

    @Test
    public void testGetFrequencyScoreNoFrequencyDataSet() {
        assertThat(instance.getFrequencyScore(), equalTo(1f));
    }
    
    @Test
    public void testThatTheConstructorCreatesAnEmptyPathogenicityDataObject() {
        PathogenicityData pathogenicityData = instance.getPathogenicityData();
        assertThat(pathogenicityData, equalTo(PathogenicityData.empty()));
        assertThat(pathogenicityData.hasPredictedScore(), is(false));
    }
    
    @Test
    public void testThatTheBuilderCanSetAPathogenicityDataObject() {
        PathogenicityData pathData = PathogenicityData.of(PolyPhenScore.of(1.0f));
        instance = testVariantBuilder().pathogenicityData(pathData).build();
        assertThat(instance.getPathogenicityData(), equalTo(pathData));
    }

    @Test
    public void testCanSetPathogenicityDataAfterConstruction() {
        PathogenicityData pathData = PathogenicityData.of(PolyPhenScore.of(1.0f));
        instance.setPathogenicityData(pathData);
        assertThat(instance.getPathogenicityData(), equalTo(pathData));
    }

    @Test
    public void testGetPathogenicityScoreUnknownVariantEffectNoPathogenicityPredictions() {
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
        assertThat(instance.getPathogenicityScore(), equalTo(0f));
    }

    @Test
    public void testGetPathogenicityScoreNonMissenseVariantNoPredictions() {
        VariantEffect type = VariantEffect.DOWNSTREAM_GENE_VARIANT;
        instance = testVariantBuilder().variantEffect(type).build();

        float expected = VariantEffectPathogenicityScore.getPathogenicityScoreOf(type);
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScoreNonMissenseVariantWithPredictions() {
        VariantEffect type = VariantEffect.REGULATORY_REGION_VARIANT;
        PathogenicityData pathData = PathogenicityData.of(CaddScore.of(1f));
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        assertThat(instance.getPathogenicityScore(), equalTo(pathData.getScore()));
    }

    @Test
    public void testGetPathogenicityScoreMissenseVariantNoPredictions() {
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().variantEffect(type).build();

        float expected = VariantEffectPathogenicityScore.getPathogenicityScoreOf(type);
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScoreMissenseSiftPass() {
        PathogenicityData pathData = PathogenicityData.of(POLYPHEN_FAIL, MTASTER_FAIL, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        assertThat(instance.getPathogenicityScore(), equalTo(SIFT_PASS.getScore()));
    }

    @Test
    public void testGetPathogenicityScoreMissensePolyPhenAndSiftPass() {
        PathogenicityData pathData = PathogenicityData.of(POLYPHEN_PASS, MTASTER_FAIL, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        assertThat(instance.getPathogenicityScore(), equalTo(SIFT_PASS.getScore()));
    }

    @Test
    public void testGetPathogenicityScoreMissensePolyPhenSiftAndMutTasterPass() {
        PathogenicityData pathData = PathogenicityData.of(POLYPHEN_PASS, MTASTER_PASS, SIFT_PASS);
        VariantEffect type = VariantEffect.MISSENSE_VARIANT;
        instance = testVariantBuilder().pathogenicityData(pathData).variantEffect(type).build();

        float expected = MTASTER_PASS.getScore();
        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetPathogenicityScoreMissensePredictedScoreLessThanDefault() {
        float expected = 0.1f;
        assertThat(expected, lessThan(VariantEffectPathogenicityScore.DEFAULT_MISSENSE_SCORE));

        PathogenicityData pathData = PathogenicityData.of(PolyPhenScore.of(expected));
        VariantEvaluation instance = testVariantBuilder()
                .pathogenicityData(pathData)
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();

        assertThat(instance.getPathogenicityScore(), equalTo(expected));
    }

    @Test
    public void testGetFailedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void testGetFailedFilterTypesDontContainPassedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        instance.addFilterResult(PASS_QUALITY_RESULT);

        assertThat(instance.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void failedFilterTypesForModeAutosomalDominantPassesFilters() {
        instance.addFilterResult(PASS_FREQUENCY_RESULT);
        instance.setCompatibleInheritanceModes(ImmutableSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));
        assertThat(instance.getFailedFilterTypesForMode(ModeOfInheritance.ANY), equalTo(Collections.emptySet()));
        assertThat(instance.getFailedFilterTypesForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(Collections.emptySet()));
        assertThat(instance.getFailedFilterTypesForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(EnumSet.of(FilterType.INHERITANCE_FILTER)));
        assertThat(instance.getFailedFilterTypesForMode(ModeOfInheritance.MITOCHONDRIAL), equalTo(EnumSet.of(FilterType.INHERITANCE_FILTER)));
    }

    @Test
    public void testBuilderFilterResultsGetFailedFilterTypesDontContainPassedFilterTypes() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());
        VariantEvaluation variantEvaluation = testVariantBuilder()
                .filterResults(FAIL_FREQUENCY_RESULT)
                .build();

        assertThat(variantEvaluation.getFailedFilterTypes(), equalTo(expectedFilters));
    }

    @Test
    public void testBuilderFilterResultsAddPassAndFailedFilters() {
        Set<FilterType> expectedFilters = EnumSet.of(FAIL_FREQUENCY_RESULT.getFilterType());
        Set<FilterType> passedFilters = EnumSet.of(PASS_QUALITY_RESULT.getFilterType());

        VariantEvaluation variantEvaluation = testVariantBuilder()
                .filterResults(FAIL_FREQUENCY_RESULT, PASS_QUALITY_RESULT)
                .build();

        assertThat(variantEvaluation.getFailedFilterTypes(), equalTo(expectedFilters));
        assertThat(variantEvaluation.getPassedFilterTypes(), equalTo(passedFilters));
    }

    @Test
    public void testGetVariantScoreWithEmptyFreqAndPathData() {
        instance = testVariantBuilder()
                .frequencyData(FrequencyData.empty())
                .pathogenicityData(PathogenicityData.empty())
                .build();
        assertThat(instance.getVariantScore(), equalTo(0f));
    }

    @Test
    public void testVariantScoreIsIndependentOfFilterStatus() {
        instance = testVariantBuilder()
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .frequencyData(FrequencyData.empty())
                //PolyPhen of 1 is predicted as highly pathogenic
                .pathogenicityData(PathogenicityData.of(PolyPhenScore.of(1f)))
                .build();
        assertThat(instance.getVariantScore(), equalTo(1f));
        assertThat(instance.passedFilters(), is(true));

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.getVariantScore(), equalTo(1f));
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassesFiltersWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(true));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testFailsFiltersWhenFailedFilterResultAdded() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testPassesFiltersWhenOnlyPassedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(PASS_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(true));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(false));
        assertThat(instance.passedFilters(), is(true));
    }

    @Test
    public void testFailsFiltersWhenPassedAndFailedFiltersHaveBeenApplied() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFailedFilterTypes().isEmpty(), is(false));
        assertThat(instance.getPassedFilterTypes().isEmpty(), is(false));
        assertThat(instance.passedFilters(), is(false));
    }

    @Test
    public void testFilterStatusWhenNoFiltersHaveBeenApplied() {
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.UNFILTERED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedAndPassed() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.PASSED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedAndFailed() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void testFilterStatusWhenFiltersHaveBeenAppliedWithPassAndFailedResults() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        assertThat(instance.getFilterStatus(), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void filterStatusForModePassedAutosomalDominantOnly() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.setCompatibleInheritanceModes(ImmutableSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT));

        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.ANY), equalTo(FilterStatus.PASSED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(FilterStatus.PASSED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void filterStatusForModePassedAutosomalDominantAndRecessive() {
        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.setCompatibleInheritanceModes(ImmutableSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.ANY), equalTo(FilterStatus.PASSED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(FilterStatus.PASSED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(FilterStatus.PASSED));
    }

    @Test
    public void filterStatusForModePassedNotFiltered() {
        instance.addFilterResult(PASS_QUALITY_RESULT);

        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.ANY), equalTo(FilterStatus.PASSED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(FilterStatus.FAILED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void filterStatusForModeUnFiltered() {
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.ANY), equalTo(FilterStatus.UNFILTERED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(FilterStatus.UNFILTERED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(FilterStatus.UNFILTERED));
    }

    @Test
    public void filterStatusForFailedModeAutosomalDominantOnly() {
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);
        instance.setCompatibleInheritanceModes(ImmutableSet.of(ModeOfInheritance.AUTOSOMAL_RECESSIVE));

        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.ANY), equalTo(FilterStatus.FAILED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), equalTo(FilterStatus.FAILED));
        assertThat(instance.getFilterStatusForMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), equalTo(FilterStatus.FAILED));
    }

    @Test
    public void testPassesFilterIsTrueWhenPassedFilterResultAdded() {
        FilterType passedFilterType = PASS_QUALITY_RESULT.getFilterType();

        instance.addFilterResult(PASS_QUALITY_RESULT);
        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.passedFilter(passedFilterType), is(true));
    }

    @Test
    public void testPassesFilterIsFalseWhenFailedFilterResultAdded() {
        FilterType filterType = FAIL_FREQUENCY_RESULT.getFilterType();

        instance.addFilterResult(FAIL_FREQUENCY_RESULT);

        assertThat(instance.passedFilter(filterType), is(false));
    }

    @Test
    public void testHasAnnotationsIsFalseByDefault() {
        assertThat(instance.hasTranscriptAnnotations(), is(false));
    }
    
    @Test
    public void testGetChromosomeName23isX() {
        instance = VariantEvaluation.builder(23, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("X"));
    }

    @Test
    public void testGetChromosomeName24isY() {
        instance = VariantEvaluation.builder(24, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("Y"));
    }

    @Test
    public void testGetChromosomeName25isMT() {
        instance = VariantEvaluation.builder(25, 1, "A", "T").build();
        assertThat(instance.getChromosomeName(), equalTo("MT"));
    }

    @Test
    public void getVariantContext() {
        VariantContext builtContext = instance.getVariantContext();
        assertThat(builtContext.getContig(), equalTo(CHROMOSOME_NAME));
        assertThat(builtContext.getStart(), equalTo(POSITION));
        assertThat(builtContext.getEnd(), equalTo(POSITION));
        assertThat(builtContext.getNAlleles(), equalTo(2));
        assertThat(builtContext.getReference().getBaseString(), equalTo(instance.getRef()));
        assertThat(builtContext.getAlternateAllele(instance.getAltAlleleId()).getBaseString(), equalTo(instance.getAlt()));
    }

    @Test
    public void testBuilderVariantContext() {
        VariantContext variantContext = new VariantContextBuilder()
                .source("Unknown")
                .chr("M").start(1).stop(1).alleles("A", "T")
                .genotypes(GenotypesContext.create(1))
                .make();
        VariantEvaluation variantEvaluation = VariantEvaluation.builder(25, 1, "A", "T")
                .variantContext(variantContext)
                .build();
        assertThat(variantEvaluation.getVariantContext(), equalTo(variantContext));
    }

    @Test
    public void getAltAlleleIdEqualsZeroWhenNotSet() {
        assertThat(instance.getAltAlleleId(), equalTo(0));
    }

    @Test
    public void getAltAlleleIdEqualsBuilderValue() {
        int altAlleleId = 2;
        instance = testVariantBuilder().altAlleleId(altAlleleId).build();
        assertThat(instance.getAltAlleleId(), equalTo(altAlleleId));
    }

    @Test
    public void testGetVariantEffectDefaultValue() {
        assertThat(instance.getVariantEffect(), equalTo(VariantEffect.SEQUENCE_VARIANT));
    }

    @Test
    public void testIsPredictedPathogenicFalseByDefault() {
        assertThat(instance.isPredictedPathogenic(), is(false));
    }

    @Test
    public void testIsPredictedPathogenicMissenseVariant() {
        instance = testVariantBuilder().variantEffect(VariantEffect.MISSENSE_VARIANT).build();
        assertThat(instance.isPredictedPathogenic(), is(true));
    }

    @Test
    public void testStopGainVariantIsPredictedPathogenicIsTrue() {
        instance = testVariantBuilder().variantEffect(VariantEffect.STOP_GAINED).build();
        assertThat(instance.isPredictedPathogenic(), is(true));
    }

    @Test
    public void testDownstreamVariantIsPredictedPathogenicIsFalse() {
        instance = testVariantBuilder().variantEffect(VariantEffect.DOWNSTREAM_GENE_VARIANT).build();
        assertThat(instance.isPredictedPathogenic(), is(false));
    }

    @Test
    public void testDoesNotContributeToGeneScoreByDefault() {
        instance = testVariantBuilder().build();
        assertThat(instance.contributesToGeneScore(), is(false));
    }

    @Test
    public void testCanSetAsContributingToGeneScoreUnderMode() {
        instance = testVariantBuilder().build();
        instance.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        instance.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        assertThat(instance.contributesToGeneScore(), is(true));
        assertThat(instance.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.contributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.contributesToGeneScoreUnderMode(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(instance.contributesToGeneScoreUnderMode(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.contributesToGeneScoreUnderMode(ModeOfInheritance.MITOCHONDRIAL), is(false));
    }

    @Test
    public void testCompatibleInheritanceModes() {
        Set<ModeOfInheritance> compatibleModes = EnumSet.of(ModeOfInheritance.AUTOSOMAL_DOMINANT, ModeOfInheritance.AUTOSOMAL_RECESSIVE);
        instance.setCompatibleInheritanceModes(compatibleModes);
        assertThat(instance.getCompatibleInheritanceModes(), equalTo(compatibleModes));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_RECESSIVE), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.AUTOSOMAL_DOMINANT), is(true));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_DOMINANT), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.X_RECESSIVE), is(false));
        assertThat(instance.isCompatibleWith(ModeOfInheritance.MITOCHONDRIAL), is(false));

        assertThat(instance.isCompatibleWith(ModeOfInheritance.ANY), is(true));
    }
    
    @Test
    public void testCompareTo() {
        //variants are sorted according to chromosome, position  ref and alt.
        VariantEvaluation zero = VariantEvaluation.builder(1, 1, "A", "C").build();
        VariantEvaluation one = VariantEvaluation.builder(1, 2, "A", "G").build();
        VariantEvaluation two = VariantEvaluation.builder(1, 2, "AC", "G").build();
        VariantEvaluation three = VariantEvaluation.builder(2, 1, "C", "T").build();
        VariantEvaluation four = VariantEvaluation.builder(2, 1, "C", "TT").build();

        List<VariantEvaluation> variants = Arrays.asList(zero, one, two, three, four);
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("chr: %2d pos: %2d ref: %-2s alt: %-2s%n", variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        Collections.sort(variants);

        List<VariantEvaluation> expected = Arrays.asList(zero, one, two, three, four);

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("chr: %2d pos: %2d ref: %-2s alt: %-2s%n", variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(expected));
    }

    private List<VariantEvaluation> scoredVariantsInDescendingRankOrder() {
        VariantEvaluation zero = VariantEvaluation.builder(2, 1, "C", "TT")
                .variantEffect(VariantEffect.FRAMESHIFT_VARIANT)
                .pathogenicityData(PathogenicityData.of(PolyPhenScore.of(1.0f)))
                .build();
        zero.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        VariantEvaluation one = VariantEvaluation.builder(2, 1, "C", "T")
                .variantEffect(VariantEffect.STOP_GAINED)
                .frequencyData(FrequencyData.of(RsId.empty(), Frequency.of(FrequencySource.ESP_ALL, 0.02f)))
                .pathogenicityData(PathogenicityData.of(PolyPhenScore.of(1.0f)))
                .build();
        one.setContributesToGeneScoreUnderMode(ModeOfInheritance.AUTOSOMAL_DOMINANT);
        VariantEvaluation two = VariantEvaluation.builder(1, 2, "A", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        VariantEvaluation three = VariantEvaluation.builder(1, 2, "AC", "G")
                .variantEffect(VariantEffect.MISSENSE_VARIANT)
                .build();
        VariantEvaluation four = VariantEvaluation.builder(1, 1, "A", "C")
                .variantEffect(VariantEffect.CODING_TRANSCRIPT_INTRON_VARIANT)
                .build();

        return Arrays.asList(zero, one, two, three, four);
    }

    @Test
    public void testVariantRankComparator() {
        //variants are sorted according to whether they are contributing to the gene score, variant score, position  ref and alt.
        List<VariantEvaluation> variants = scoredVariantsInDescendingRankOrder();
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        variants.sort(new VariantEvaluation.RankBasedComparator());

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(scoredVariantsInDescendingRankOrder()));
    }

    @Test
    public void testVariantCompareByRank() {
        //variants are sorted according to whether they are contributing to the gene score, variant score, position  ref and alt.
        List<VariantEvaluation> variants = scoredVariantsInDescendingRankOrder();
        Collections.shuffle(variants);

        System.out.println("Shuffled:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));

        variants.sort(VariantEvaluation::compareByRank);

        System.out.println("Sorted:");
        variants.forEach(variant -> System.out.printf("%s score: %3f chr: %2d pos: %2d ref: %-2s alt: %-2s%n", (variant.contributesToGeneScore() ? '*' : ' '), variant
                .getVariantScore(), variant.getChromosome(), variant.getPosition(), variant.getRef(), variant.getAlt()));
        assertThat(variants, equalTo(scoredVariantsInDescendingRankOrder()));
    }

    @Test
    public void testToString() {
        String expected = "VariantEvaluation{assembly=hg19 chr=1 pos=1 ref=C alt=T qual=2.2 SEQUENCE_VARIANT score=0.0 UNFILTERED failedFilters=[] passedFilters=[] compatibleWith=[] sampleGenotypes={sample=0/1}}";
        System.out.println(instance);
        assertThat(instance.toString(), equalTo(expected));
    }

    @Test
    public void testToStringVariantContributesToGeneScore() {
        String expected = "VariantEvaluation{assembly=hg19 chr=1 pos=1 ref=C alt=T qual=2.2 SEQUENCE_VARIANT * score=0.0 UNFILTERED failedFilters=[] passedFilters=[] compatibleWith=[] sampleGenotypes={sample=0/1}}";
        instance.setContributesToGeneScoreUnderMode(ModeOfInheritance.ANY);
        System.out.println(instance);
        assertThat(instance.toString(), equalTo(expected));
    }

    @Test
    void variantIsNotWhiteListedByDefault() {
        assertThat(instance.isWhiteListed(), is(false));
    }

    @Test
    void whiteListStatusIsMutable() {
        VariantEvaluation instance = testVariantBuilder()
                .whiteListed(false)
                .build();
        assertThat(instance.isWhiteListed(), is(false));

        instance.setWhiteListed(true);
        assertThat(instance.isWhiteListed(), is(true));
    }

    @Test
    void whiteListedVariantsAlwaysHaveMaximalVariantScore() {
        // this is well above the default AD frequency cut-off
        instance.setFrequencyData(FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_G_SAS, 3f)));
        instance.setPathogenicityData(PathogenicityData.empty());

        assertThat(instance.getVariantScore(), equalTo(0f));

        instance.setWhiteListed(true);

        assertThat(instance.getVariantScore(), equalTo(1f));
    }

    @Test
    void whiteListedVariantsAlwaysHaveMaximalFrequencyScore() {
        // this is well above the default AD frequency cut-off
        instance.setFrequencyData(FrequencyData.of(Frequency.of(FrequencySource.GNOMAD_G_SAS, 3f)));
        assertThat(instance.getFrequencyScore(), equalTo(0f));

        instance.setWhiteListed(true);
        assertThat(instance.getFrequencyScore(), equalTo(1f));
    }

    @Test
    void whiteListedVariantsAlwaysHaveMaximalPathogenicityScore() {
        instance.setPathogenicityData(PathogenicityData.empty());
        assertThat(instance.getPathogenicityScore(), equalTo(0f));
        assertThat(instance.isPredictedPathogenic(), equalTo(false));

        instance.setWhiteListed(true);

        assertThat(instance.getPathogenicityScore(), equalTo(1f));
        assertThat(instance.isPredictedPathogenic(), equalTo(true));
    }
}
