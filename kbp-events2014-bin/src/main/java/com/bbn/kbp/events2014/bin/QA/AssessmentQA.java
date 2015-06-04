package com.bbn.kbp.events2014.bin.QA;

import com.bbn.bue.common.parameters.Parameters;
import com.bbn.bue.common.symbols.Symbol;
import com.bbn.kbp.events2014.AnswerKey;
import com.bbn.kbp.events2014.Response;
import com.bbn.kbp.events2014.TypeRoleFillerRealis;
import com.bbn.kbp.events2014.bin.QA.Warnings.ConflictingTypeWarningRule;
import com.bbn.kbp.events2014.bin.QA.Warnings.ConjunctionWarningRule;
import com.bbn.kbp.events2014.bin.QA.Warnings.OverlapWarningRule;
import com.bbn.kbp.events2014.bin.QA.Warnings.Warning;
import com.bbn.kbp.events2014.bin.QA.Warnings.WarningRule;
import com.bbn.kbp.events2014.io.AnnotationStore;
import com.bbn.kbp.events2014.io.AssessmentSpecFormats;
import com.bbn.kbp.events2014.transformers.MakeAllRealisActual;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.io.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by jdeyoung on 6/1/15.
 */
public class AssessmentQA {

  private static final Logger log = LoggerFactory.getLogger(AssessmentQA.class);

  public static void trueMain(String... args) throws IOException {
    final Parameters params = Parameters.loadSerifStyle(new File(args[0]));
    final AnnotationStore store =
        AssessmentSpecFormats.openAnnotationStore(params.getExistingDirectory("annotationStore"),
            AssessmentSpecFormats.Format.KBP2015);
    warnings = ImmutableList.of(ConjunctionWarningRule.create(), OverlapWarningRule.create(),
        ConflictingTypeWarningRule.create(params.getString("argFile"), params.getString("roleFile")));

    for (Symbol docID : store.docIDs()) {
      log.info("processing document {}", docID.asString());
      final AnswerKey answerKey = MakeAllRealisActual.forAnswerKey().apply(store.read(docID));
      final DocumentRenderer htmlRenderer = new DocumentRenderer(docID.asString(), overallOrdering, trfrOrdering);
      final File output = params.getCreatableDirectory("output");
      log.info("serializing {}", docID.asString());
      htmlRenderer.renderTo(
          Files.asCharSink(new File(output, docID.asString() + ".html"), Charset.defaultCharset()),
          answerKey, generateWarnings(answerKey));
    }
  }

  private static Ordering<Response> overallOrdering = Ordering.compound(ImmutableList
      .of(Response.byEvent(), Response.byRole(), Response.byFillerString(),
          Response.byCASSttring()));
  private static Ordering<TypeRoleFillerRealis> trfrOrdering = Ordering.compound(ImmutableList.of(
      TypeRoleFillerRealis.byType(), TypeRoleFillerRealis.byRole(), TypeRoleFillerRealis.byCAS(),
      TypeRoleFillerRealis.byRealis()));
  private static List<? extends WarningRule> warnings = null;



  private static ImmutableMultimap<Response, Warning> generateWarnings(
      AnswerKey answerKey) {
    ImmutableMultimap.Builder<Response, Warning> warningResponseBuilder =
        ImmutableMultimap.builder();
    for (WarningRule w : warnings) {
      Multimap<Response, Warning> warnings = w.applyWarning(answerKey);
      warningResponseBuilder.putAll(warnings);
    }
    return warningResponseBuilder.build();
  }

  public static String readableTRFR(TypeRoleFillerRealis trfr) {
    return String.format("%s-%s:%s - %s", trfr.type().asString(), trfr.role().asString(),
        trfr.realis().name(), trfr.argumentCanonicalString().string());
  }

  public static void main(String... args) {
    try {
      trueMain(args);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }


}
