//
// Created by MAC OS on 28/08/2018.
//

#include "PasswordGenerator.h"
#include "openssl/md5.h"
#include <string.h>
#include "Util.h"

#include <sstream>
using namespace std;


const char sLetters[] = {'g','d','o', 'f','r', 'q', 's', 'e', 'p', 'n', 'w', 'u','c', 't', 'v', 'a', 'k', 'j','b', 'y','m', 'h', 'i', 'x', 'z','l'};
const char sNums[]={'0', '3',  '2', '4','6',  '1', '8','7', '9', '5'};
const char sSymbols[]={'-', '_'};

const int sNumCount = sizeof(sNums)/ sizeof(sNums[0]);
const int sLetterCount = sizeof(sLetters)/ sizeof(sLetters[0]);
const int sSymbolCount = sizeof(sSymbols)/ sizeof(sSymbols[0]);

static const int TYPE_NUMBER = 1;
static const int TYPE_ABC = 2;
static const int TYPE_NUMBER_ABC = 3;
static const int TYPE_NUMBER_ABC_SYMBOL = 4;


PasswordGenerator::PasswordGenerator(char *key, char *name): mKey(key), mName(name),mVer(1),mType(TYPE_NUMBER_ABC) {

}

std::string PasswordGenerator::generate() {

    unsigned char digest[MD5_DIGEST_LENGTH];

    std::ostringstream s;
    s <<  mName << ":" << mKey << ":" << mVer;
    std::string tmp(s.str());

    const char* str = tmp.c_str();

    Util::logger("str=%s\n",str);

    MD5((unsigned char*)str, strlen(str), (unsigned char*)&digest);

    char mdString[33];
    for(int i = 0; i < 16; i++)
        sprintf(&mdString[i*2], "%02x", (unsigned int)digest[i]);

    Util::logger("md5=%s\n", mdString);

    char szPass[9]="\0";

    for(int i = 0; i < 8; i++){
        unsigned int idx = (unsigned int)digest[2*i] + (unsigned int)digest[2*i+1];
        switch (mType){
            case TYPE_ABC:
                szPass[i] = sLetters[idx % sLetterCount];
                break;
            case TYPE_NUMBER:
                szPass[i] = sNums[idx % sNumCount];
                break;
            case TYPE_NUMBER_ABC:
                if (i < 3){
                    szPass[i] = sLetters[idx % sLetterCount];
                } else {
                    szPass[i] = sNums[idx % sNumCount];
                }
                break;
            case TYPE_NUMBER_ABC_SYMBOL:
                if (i < 2){
                    szPass[i] = sLetters[idx % sLetterCount];
                } else if (i >=3){
                    szPass[i] = sNums[idx % sNumCount];
                } else {
                    szPass[i] = sSymbols[idx % sSymbolCount];
                }
                break;
            default:
                break;
        }
    }


    return string(szPass);
}

PasswordGenerator::~PasswordGenerator() {

}
