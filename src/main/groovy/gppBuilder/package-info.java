/**
 * The program GPPbuilder takes a source file that uses components from the GPP Library
 * and transforms them into a runnable Groovy script.<p>
 * The program expects a single argument which is the fullpath name of the source file
 * excluding the required suffix "_gpp.groovy".  The output from the program is a file with
 * the suffix ".groovy".<p>
 * If any errors are found in the source file the output file will contain an indication
 * of the error at the place where it was identified.<p>
 * The source file contains all the data definition and Detail objects required by the application<p>.
 *
 * The processes that comprise the application process network must be specified in the order in
 * which the data flows through the process network.<p>
 *
 *
 *
 */

package gppBuilder;