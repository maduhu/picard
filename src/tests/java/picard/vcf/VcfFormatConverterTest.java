/*
* Copyright (c) 2013 The Broad Institute
*
* Permission is hereby granted, free of charge, to any person
* obtaining a copy of this software and associated documentation
* files (the "Software"), to deal in the Software without
* restriction, including without limitation the rights to use,
* copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the
* Software is furnished to do so, subject to the following
* conditions:
*
* The above copyright notice and this permission notice shall be
* included in all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
* EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
* OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
* HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
* WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
* FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
* THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

package picard.vcf;

import htsjdk.tribble.Tribble;
import org.testng.Assert;
import org.testng.annotations.Test;
import picard.PicardException;

import java.io.File;
import java.io.IOException;

public class VcfFormatConverterTest {
    private static final String TEST_DATA_PATH = "testdata/picard/vcf/";
    private static final String TEST_FILE_BASE = "vcfFormatTest";

    private static final String VCF = ".vcf";
    private static final String VCF_GZ = ".vcf.gz";
	private static final String BCF = ".bcf";

    private static final File TEST_VCF = new File(TEST_DATA_PATH, TEST_FILE_BASE + VCF);
    private static final File TEST_BCF = new File(TEST_DATA_PATH, TEST_FILE_BASE + BCF);

    @Test
    public void testVcfToVcf() {
        runLikeTest(TEST_VCF, VCF);
    }

    @Test
    public void testVcfToBcf() {
        runBackAndForthTest(TEST_VCF, BCF, VCF);
    }

    @Test
    public void testVcfToVcfGz() {
        runBackAndForthTest(TEST_VCF, VCF_GZ, VCF);
    }

    @Test
    public void testBcfToBcf() {
        runLikeTest(TEST_BCF, BCF);
    }

    @Test
    public void testBcfToVcf() {
        runBackAndForthTest(TEST_BCF, VCF, BCF);
    }

    private void runLikeTest(final File input, final String format) {
        final File outputFile = convertFile(input, "likeTest", format);
        compareFiles(input, outputFile);
    }

    private void runBackAndForthTest(final File input, final String format, final String originalFormat) {
        final String tempPrefix = "backAndForth";

        final File backAndForth = convertFile(input, tempPrefix, format);
        final File backAndForthSeries2 = convertFile(backAndForth, tempPrefix, originalFormat);

        compareFiles(input, backAndForthSeries2);
    }

    private File convertFile(final File input, final String prefix, final String format) {
        final File outputFile;
        try {
            outputFile = File.createTempFile(prefix, format);
        } catch (final IOException ioe) {
            throw new PicardException("Unable to create temp file!");
        }

        outputFile.deleteOnExit();
        new File(outputFile.getAbsolutePath() + Tribble.STANDARD_INDEX_EXTENSION).deleteOnExit();
        final VcfFormatConverter vcfFormatConverter = new VcfFormatConverter();
        vcfFormatConverter.INPUT = input;
        vcfFormatConverter.OUTPUT = outputFile;
        if (VCF_GZ.equals(format)) {
            vcfFormatConverter.CREATE_INDEX = false;
        }
        if (input.getName().endsWith(VCF_GZ)) {
            vcfFormatConverter.REQUIRE_INDEX = false;
        }

        Assert.assertEquals(vcfFormatConverter.doWork(), 0);
        return outputFile;
    }

    private void compareFiles(final File file1, final File file2) {
        // Ok, so this isn't exactly comparing md5 checksums or anything, but it should be good enough
        // for our purposes.
        Assert.assertTrue(file1.exists());
        Assert.assertTrue(file2.exists());
        Assert.assertEquals(file1.length(), file2.length());
    }

}
