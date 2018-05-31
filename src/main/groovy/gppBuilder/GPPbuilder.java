package gppBuilder;



/**
 * GPPbuilder provides a means of transforming a file containing the definition of a
 * GPP defined application, in which, the process definitions omit the communication and
 * parallel composition definitions.  The program constructs the required definitions
 * automatically.<p>
 *
 * The argument to the program is a \path\filename that has a _gpp.groovy suffix.  The
 * supplied argument should omit the suffix.  The output is the \path\filename now
 * with a .groovy suffix.  The outpfile can be executed as a groovy script.
 * Any errors will be placed in the output file at the place where they were detected.<p>
 *
 */
public class GPPbuilder {

    /**
     *
     * @param args args[0] contains the full path name of a the file to be converted, excluding the _gpp.groovy suffix
     *
     */
    public static void main(String[] args) {
        String fileRoot = args[0];
        String inFile = fileRoot +  "_gpp.groovy";
        String outFile = fileRoot + ".groovy";
        GPPlexFileHanding gppLexer = new GPPlexFileHanding();
        gppLexer.openFiles(inFile, outFile);
        System.out.println( "Transforming: " + inFile);
        System.out.println( "Into: " + outFile);
        String error = gppLexer.parse();
        if ( error == ""){
            System.out.println("Build Successful: " + outFile);
        }
        else System.out.println("Build failed:" + error);
    }

    /**
     * A method that calls the GPPbuilder program.
     *
     * @param fileRoot the name of the root of the file to be transformed without the _gpp.groovy suffix, the output
     * is a file with the a .groovy suffix that can be executed as a groovy script.
     */
    public static void runBuilder(String fileRoot){
        String inFile = fileRoot +  "_gpp.groovy";
        String outFile = fileRoot + ".groovy";
        GPPlexFileHanding gppLexer = new GPPlexFileHanding();
        gppLexer.openFiles(inFile, outFile);
        System.out.println( "Transforming: " + inFile);
        System.out.println( "Into: " + outFile);
        String error = gppLexer.parse();
        if ( error == ""){
            System.out.println("Build Successful: " + outFile);
        }
        else System.out.println("Build failed:" + error);

    }

}
