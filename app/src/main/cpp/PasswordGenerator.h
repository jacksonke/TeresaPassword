//
// Created by MAC OS on 28/08/2018.
//

#ifndef TERESAKEY_ABSGENERATOR_H
#define TERESAKEY_ABSGENERATOR_H

#include <string>

class PasswordGenerator {
private:
    std::string mKey;
    std::string mName;
    int mVer = 1;
    int mType = 1;
public:
    PasswordGenerator(char* key, char* name);
    void setVersion(int ver){mVer = ver;}
    void setType(int type){ mType = type;}
    virtual std::string generate();
    virtual  ~PasswordGenerator();
};


#endif //TERESAKEY_ABSGENERATOR_H
