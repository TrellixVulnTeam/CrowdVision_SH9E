import styled from 'styled-components'
import Colors from 'Config/Colors'

export const ProfileFormStyle = styled.div `
    margin:auto;
    width: 50%;
    
    .submit {
        background-color: ${Colors.contrast_1};
        color: black;
        font-weight:500;
        border: none;
        margin-top:10px;
        float: right;
    }
`;